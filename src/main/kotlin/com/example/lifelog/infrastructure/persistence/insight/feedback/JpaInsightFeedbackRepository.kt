package com.example.lifelog.infrastructure.persistence.insight.feedback

import com.example.lifelog.domain.insight.feedback.InsightFeedback
import org.springframework.data.jpa.repository.JpaRepository

/**
 * InsightFeedback JPA Repository
 */
interface JpaInsightFeedbackRepository : JpaRepository<InsightFeedback, Long> {
    fun existsByUserIdAndInsightId(
        userId: Long,
        insightId: Long,
    ): Boolean

    fun findByUserIdAndInsightId(
        userId: Long,
        insightId: Long,
    ): InsightFeedback?
}
