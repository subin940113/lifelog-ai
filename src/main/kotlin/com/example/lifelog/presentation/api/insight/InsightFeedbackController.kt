package com.example.lifelog.presentation.api.insight

import com.example.lifelog.application.insight.feedback.InsightFeedbackView
import com.example.lifelog.application.insight.feedback.SubmitInsightFeedbackRequest
import com.example.lifelog.application.insight.feedback.SubmitInsightFeedbackUseCase
import com.example.lifelog.infrastructure.security.AuthPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 인사이트 피드백 API Controller
 */
@RestController
@RequestMapping("/api/insights")
class InsightFeedbackController(
    private val submitInsightFeedbackUseCase: SubmitInsightFeedbackUseCase,
) {
    @PostMapping("/{insightId}/feedback")
    fun submit(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @PathVariable insightId: Long,
        @RequestBody request: SubmitInsightFeedbackRequest,
    ): InsightFeedbackView {
        val userId = principal.userId
        return submitInsightFeedbackUseCase.submit(userId = userId, insightId = insightId, request = request)
    }

    @GetMapping("/{insightId}/feedback")
    fun get(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @PathVariable insightId: Long,
    ): InsightFeedbackView? {
        val userId = principal.userId
        return submitInsightFeedbackUseCase.get(userId, insightId)
    }
}
