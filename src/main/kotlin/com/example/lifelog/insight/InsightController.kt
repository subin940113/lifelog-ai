package com.example.lifelog.insight

import com.example.lifelog.auth.security.AuthPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/insights")
class InsightController(
    private val service: AiInsightService,
) {
    @GetMapping
    fun list(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(required = false) cursor: String?,
    ): AiInsightsPageResponse {
        val userId = principal.userId
        return service.list(userId = userId, limit = limit, cursor = cursor)
    }
}
