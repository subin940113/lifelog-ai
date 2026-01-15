package com.example.lifelog.insight.feedback

import org.springframework.data.jpa.repository.JpaRepository

interface AiInsightFeedbackRepository : JpaRepository<AiInsightFeedback, Long> {
    fun existsByUserIdAndInsightId(
        userId: Long,
        insightId: Long,
    ): Boolean

    fun findByUserIdAndInsightId(
        userId: Long,
        insightId: Long,
    ): AiInsightFeedback?
}
