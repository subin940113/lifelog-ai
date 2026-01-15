package com.example.lifelog.insight.feedback

import com.example.lifelog.auth.security.AuthPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/insights")
class InsightFeedbackController(
    private val feedbackService: InsightFeedbackService,
) {
    @PostMapping("/{insightId}/feedback")
    fun submit(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @PathVariable insightId: Long,
        @RequestBody req: SubmitInsightFeedbackRequest,
    ) {
        val userId = principal.userId
        feedbackService.submit(userId = userId, insightId = insightId, req = req)
    }

    @GetMapping("/{insightId}/feedback")
    fun get(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @PathVariable insightId: Long,
    ): InsightFeedbackView? {
        val userId = principal.userId
        return feedbackService.get(userId, insightId)
    }
}
