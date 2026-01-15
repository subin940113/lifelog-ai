package com.example.lifelog.insight.feedback

import com.example.lifelog.insight.AiInsightKind
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class InsightPreferenceReader(
    private val profileRepo: InsightPreferenceProfileRepository,
    private val objectMapper: ObjectMapper,
) {
    fun kindWeight(
        userId: Long,
        kind: AiInsightKind,
    ): Double {
        val profile = profileRepo.findByUserId(userId) ?: return 0.0
        val node = objectMapper.readTree(profile.kindWeightsJson)
        return node.get(kind.name)?.asDouble(0.0) ?: 0.0
    }

    fun dislikeStreak(userId: Long): Int = profileRepo.findByUserId(userId)?.dislikeStreak ?: 0
}
