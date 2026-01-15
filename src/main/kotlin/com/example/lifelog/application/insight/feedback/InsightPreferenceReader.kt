package com.example.lifelog.application.insight.feedback

import com.example.lifelog.domain.insight.InsightKind
import com.example.lifelog.domain.insight.feedback.InsightPreferenceProfileRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

/**
 * 인사이트 선호도 읽기 컴포넌트
 */
@Component
class InsightPreferenceReader(
    private val profileRepository: InsightPreferenceProfileRepository,
    private val objectMapper: ObjectMapper,
) {
    fun getKindWeight(
        userId: Long,
        kind: InsightKind,
    ): Double {
        val profile = profileRepository.findByUserId(userId) ?: return 0.0
        val node = objectMapper.readTree(profile.kindWeightsJson)
        return node.get(kind.name)?.asDouble(0.0) ?: 0.0
    }

    fun getDislikeStreak(userId: Long): Int = profileRepository.findByUserId(userId)?.dislikeStreak ?: 0
}
