package com.example.lifelog.application.insight.feedback

import com.example.lifelog.common.exception.ErrorCode
import com.example.lifelog.common.exception.ForbiddenException
import com.example.lifelog.common.exception.NotFoundException
import com.example.lifelog.domain.insight.InsightKind
import com.example.lifelog.domain.insight.InsightRepository
import com.example.lifelog.domain.insight.feedback.FeedbackReason
import com.example.lifelog.domain.insight.feedback.FeedbackVote
import com.example.lifelog.domain.insight.feedback.InsightFeedback
import com.example.lifelog.domain.insight.feedback.InsightFeedbackRepository
import com.example.lifelog.domain.insight.feedback.InsightPreferenceProfile
import com.example.lifelog.domain.insight.feedback.InsightPreferenceProfileRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import kotlin.math.max
import kotlin.math.min

/**
 * 인사이트 피드백 제출 Use Case
 */
@Service
class SubmitInsightFeedbackUseCase(
    private val insightRepository: InsightRepository,
    private val feedbackRepository: InsightFeedbackRepository,
    private val profileRepository: InsightPreferenceProfileRepository,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(
        userId: Long,
        insightId: Long,
        request: SubmitInsightFeedbackRequest,
    ): InsightFeedbackResponse {
        val insight =
            insightRepository.findById(insightId)
                ?: throw NotFoundException(ErrorCode.NOT_FOUND_INSIGHT, "Insight not found: $insightId")

        if (insight.userId != userId) {
            throw ForbiddenException(ErrorCode.FORBIDDEN, "Access denied to insight: $insightId")
        }

        val normalizedComment =
            request.comment
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?.take(1000)

        val normalizedScore = request.score?.coerceIn(1, 5)

        val existing = feedbackRepository.findByUserIdAndInsightId(userId, insightId)

        val saved =
            if (existing != null) {
                existing.update(request.vote, request.reason, normalizedScore, normalizedComment)
                feedbackRepository.save(existing)
            } else {
                feedbackRepository.save(
                    InsightFeedback(
                        userId = userId,
                        insightId = insightId,
                        vote = request.vote,
                        reason = request.reason,
                        score = normalizedScore,
                        comment = normalizedComment,
                        createdAt = Instant.now(),
                    ),
                )
            }

        // 프로필 업데이트 (개인화 핵심)
        updatePreferenceProfile(userId, insight.kind, request.vote, request.reason)

        return InsightFeedbackResponse.from(saved)
    }

    private fun updatePreferenceProfile(
        userId: Long,
        kind: InsightKind,
        vote: FeedbackVote,
        reason: FeedbackReason?,
    ) {
        val profile =
            profileRepository.findByUserId(userId)
                ?: InsightPreferenceProfile(
                    userId = userId,
                    kindWeightsJson = defaultWeightsJson(),
                    dislikeStreak = 0,
                )

        val weights = readWeights(profile.kindWeightsJson).toMutableMap()

        // kind 가중치 조절: LIKE +0.15, DISLIKE -0.25 (보수적 추천)
        val delta = if (vote == FeedbackVote.LIKE) 0.15 else -0.25
        weights[kind] = clamp((weights[kind] ?: 0.0) + delta, -1.5, 1.5)

        // "너무 날카로움"이면 WARNING/CONTRAST를 추가로 낮춤(사용자 취향 반영)
        if (vote == FeedbackVote.DISLIKE && reason == FeedbackReason.TOO_SHARP) {
            weights[InsightKind.WARNING] = clamp((weights[InsightKind.WARNING] ?: 0.0) - 0.20, -1.5, 1.5)
            weights[InsightKind.CONTRAST] = clamp((weights[InsightKind.CONTRAST] ?: 0.0) - 0.15, -1.5, 1.5)
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

        profileRepository.save(profile)
    }

    private fun clamp(
        v: Double,
        lo: Double,
        hi: Double,
    ): Double = max(lo, min(hi, v))

    private fun defaultWeightsJson(): String =
        writeWeights(
            mapOf(
                InsightKind.PATTERN to 0.0,
                InsightKind.HIGHLIGHT to 0.0,
                InsightKind.WARNING to -0.1, // 기본값: WARNING 과생산 억제
                InsightKind.CONTRAST to -0.05,
                InsightKind.REFLECTION to 0.0,
                InsightKind.QUESTION to -0.1,
                InsightKind.TENDENCY to 0.0,
            ),
        )

    private fun readWeights(json: String): Map<InsightKind, Double> {
        val node = objectMapper.readTree(json)
        val map = mutableMapOf<InsightKind, Double>()
        node.fields().forEach { (k, v) ->
            runCatching { InsightKind.valueOf(k) }.getOrNull()?.let { map[it] = v.asDouble(0.0) }
        }
        return map
    }

    private fun writeWeights(map: Map<InsightKind, Double>): String {
        val obj = objectMapper.createObjectNode()
        map.forEach { (k, v) -> obj.put(k.name, v) }
        return objectMapper.writeValueAsString(obj)
    }
}

/**
 * 인사이트 피드백 제출 요청 DTO
 */
data class SubmitInsightFeedbackRequest(
    val vote: FeedbackVote,
    val reason: FeedbackReason? = null,
    val score: Int? = null, // 1~5 optional
    val comment: String? = null,
)

/**
 * 인사이트 피드백 응답
 */
data class InsightFeedbackResponse(
    val insightId: Long,
    val vote: FeedbackVote,
    val reason: FeedbackReason?,
    val comment: String?,
    val updatedAt: Instant,
) {
    companion object {
        fun from(entity: InsightFeedback): InsightFeedbackResponse =
            InsightFeedbackResponse(
                insightId = entity.insightId,
                vote = entity.vote,
                reason = entity.reason,
                comment = entity.comment,
                updatedAt = entity.updatedAt,
            )
    }
}
