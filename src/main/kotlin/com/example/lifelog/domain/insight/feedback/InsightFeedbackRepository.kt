package com.example.lifelog.domain.insight.feedback

/**
 * Insight feedback 도메인 리포지토리 인터페이스
 */
interface InsightFeedbackRepository {
    fun existsByUserIdAndInsightId(
        userId: Long,
        insightId: Long,
    ): Boolean

    fun findByUserIdAndInsightId(
        userId: Long,
        insightId: Long,
    ): InsightFeedback?

    fun save(feedback: InsightFeedback): InsightFeedback
}
