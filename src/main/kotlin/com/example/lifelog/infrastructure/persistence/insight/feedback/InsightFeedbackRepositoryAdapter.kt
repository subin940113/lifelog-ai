package com.example.lifelog.infrastructure.persistence.insight.feedback

import com.example.lifelog.domain.insight.feedback.InsightFeedback
import com.example.lifelog.domain.insight.feedback.InsightFeedbackRepository
import org.springframework.stereotype.Component

/**
 * InsightFeedbackRepository JPA 어댑터
 */
@Component
class InsightFeedbackRepositoryAdapter(
    private val jpaRepository: JpaInsightFeedbackRepository,
) : InsightFeedbackRepository {
    override fun existsByUserIdAndInsightId(
        userId: Long,
        insightId: Long,
    ): Boolean = jpaRepository.existsByUserIdAndInsightId(userId, insightId)

    override fun findByUserIdAndInsightId(
        userId: Long,
        insightId: Long,
    ): InsightFeedback? = jpaRepository.findByUserIdAndInsightId(userId, insightId)

    override fun save(feedback: InsightFeedback): InsightFeedback = jpaRepository.save(feedback)
}
