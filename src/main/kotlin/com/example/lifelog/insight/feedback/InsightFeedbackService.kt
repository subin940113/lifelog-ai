package com.example.lifelog.insight.feedback

import com.example.lifelog.insight.AiInsightKind
import com.example.lifelog.insight.AiInsightRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import kotlin.math.max
import kotlin.math.min

data class SubmitInsightFeedbackRequest(
    val vote: FeedbackVote,
    val reason: FeedbackReason? = null,
    val score: Int? = null, // 1~5 optional
    val comment: String? = null,
)

data class InsightFeedbackView(
    val insightId: Long,
    val vote: FeedbackVote,
    val reason: FeedbackReason?,
    val comment: String?,
    val updatedAt: Instant,
) {
    companion object {
        fun from(entity: AiInsightFeedback): InsightFeedbackView =
            InsightFeedbackView(
                insightId = entity.insightId,
                vote = entity.vote,
                reason = entity.reason,
                comment = entity.comment,
                updatedAt = entity.updatedAt,
            )
    }
}

@Service
class InsightFeedbackService(
    private val insightRepo: AiInsightRepository,
    private val feedbackRepo: AiInsightFeedbackRepository,
    private val profileRepo: InsightPreferenceProfileRepository,
    private val objectMapper: ObjectMapper,
) {
    @Transactional(readOnly = true)
    fun get(
        userId: Long,
        insightId: Long,
    ): InsightFeedbackView? =
        feedbackRepo
            .findByUserIdAndInsightId(userId, insightId)
            ?.let(InsightFeedbackView::from)

    @Transactional
    fun submit(
        userId: Long,
        insightId: Long,
        req: SubmitInsightFeedbackRequest,
    ): InsightFeedbackView {
        val insight =
            insightRepo.findById(insightId).orElseThrow {
                IllegalArgumentException("Insight not found")
            }

        if (insight.userId != userId) {
            throw IllegalArgumentException("Forbidden")
        }

        val now = Instant.now()

        val normalizedComment =
            req.comment
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?.take(1000)

        val normalizedScore = req.score?.coerceIn(1, 5)

        val existing = feedbackRepo.findByUserIdAndInsightId(userId, insightId)

        val saved =
            if (existing != null) {
                existing.update(req.vote, req.reason, normalizedScore, normalizedComment)
                feedbackRepo.save(existing)
            } else {
                feedbackRepo.save(
                    AiInsightFeedback(
                        userId = userId,
                        insightId = insightId,
                        vote = req.vote,
                        reason = req.reason,
                        score = normalizedScore,
                        comment = normalizedComment,
                        createdAt = now,
                    ),
                )
            }

        // 프로필 업데이트 (개인화 핵심)
        updatePreferenceProfile(userId, insight.kind, req.vote, req.reason)

        return InsightFeedbackView.from(saved)
    }

    private fun updatePreferenceProfile(
        userId: Long,
        kind: AiInsightKind,
        vote: FeedbackVote,
        reason: FeedbackReason?,
    ) {
        val profile =
            profileRepo.findByUserId(userId)
                ?: InsightPreferenceProfile(
                    userId = userId,
                    kindWeightsJson = defaultWeightsJson(),
                    dislikeStreak = 0,
                )

        val weights = readWeights(profile.kindWeightsJson).toMutableMap()

        // kind 가중치 조절: LIKE +0.15, DISLIKE -0.25 (보수적 추천)
        val delta = if (vote == FeedbackVote.LIKE) 0.15 else -0.25
        weights[kind] = clamp((weights[kind] ?: 0.0) + delta, -1.5, 1.5)

        // “너무 날카로움”이면 WARNING/CONTRAST를 추가로 낮춤(사용자 취향 반영)
        if (vote == FeedbackVote.DISLIKE && reason == FeedbackReason.TOO_SHARP) {
            weights[AiInsightKind.WARNING] = clamp((weights[AiInsightKind.WARNING] ?: 0.0) - 0.20, -1.5, 1.5)
            weights[AiInsightKind.CONTRAST] = clamp((weights[AiInsightKind.CONTRAST] ?: 0.0) - 0.15, -1.5, 1.5)
        }

        // dislike streak (쿨다운/발생 빈도 조절용)
        profile.dislikeStreak =
            if (vote == FeedbackVote.DISLIKE) {
                min(profile.dislikeStreak + 1, 10)
            } else {
                max(profile.dislikeStreak - 1, 0)
            }

        profile.kindWeightsJson = writeWeights(weights)
        profile.updatedAt = Instant.now()

        profileRepo.save(profile)
    }

    private fun clamp(
        v: Double,
        lo: Double,
        hi: Double,
    ): Double = max(lo, min(hi, v))

    private fun defaultWeightsJson(): String =
        writeWeights(
            mapOf(
                AiInsightKind.PATTERN to 0.0,
                AiInsightKind.HIGHLIGHT to 0.0,
                AiInsightKind.WARNING to -0.1, // 기본값: WARNING 과생산 억제
                AiInsightKind.CONTRAST to -0.05,
                AiInsightKind.REFLECTION to 0.0,
                AiInsightKind.QUESTION to -0.1,
                AiInsightKind.TENDENCY to 0.0,
            ),
        )

    private fun readWeights(json: String): Map<AiInsightKind, Double> {
        val node = objectMapper.readTree(json)
        val map = mutableMapOf<AiInsightKind, Double>()
        node.fields().forEach { (k, v) ->
            runCatching { AiInsightKind.valueOf(k) }.getOrNull()?.let { map[it] = v.asDouble(0.0) }
        }
        return map
    }

    private fun writeWeights(map: Map<AiInsightKind, Double>): String {
        val obj = objectMapper.createObjectNode()
        map.forEach { (k, v) -> obj.put(k.name, v) }
        return objectMapper.writeValueAsString(obj)
    }
}
