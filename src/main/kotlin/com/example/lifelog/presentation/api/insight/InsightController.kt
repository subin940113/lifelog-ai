package com.example.lifelog.presentation.api.insight

import com.example.lifelog.application.insight.ListInsightsUseCase
import com.example.lifelog.infrastructure.security.AuthPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * 인사이트 API Controller
 */
@RestController
@RequestMapping("/api/insights")
class InsightController(
    private val listInsightsUseCase: ListInsightsUseCase,
) {
    @GetMapping
    fun list(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(required = false) cursor: String?,
    ) = listInsightsUseCase.execute(
        userId = principal.userId,
        limit = limit,
        cursor = cursor,
    )
}
