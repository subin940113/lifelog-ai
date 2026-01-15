package com.example.lifelog.application.insight.feedback

import com.example.lifelog.domain.insight.feedback.InsightFeedbackRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 인사이트 피드백 조회 Use Case
 */
@Service
class GetInsightFeedbackUseCase(
    private val feedbackRepository: InsightFeedbackRepository,
) {
    @Transactional(readOnly = true)
    fun execute(
        userId: Long,
        insightId: Long,
    ): InsightFeedbackResponse? {
        val feedback = feedbackRepository.findByUserIdAndInsightId(userId, insightId)
        return feedback?.let { InsightFeedbackResponse.from(it) }
    }
}
