package com.example.lifelog.presentation.api.insight

import com.example.lifelog.application.insight.settings.InsightSettingsResponse
import com.example.lifelog.application.insight.settings.ManageInsightSettingsUseCase
import com.example.lifelog.infrastructure.security.AuthPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 인사이트 설정 API Controller
 */
@RestController
@RequestMapping("/api/insights/settings")
class InsightSettingsController(
    private val manageInsightSettingsUseCase: ManageInsightSettingsUseCase,
) {
    @GetMapping
    fun get(
        @AuthenticationPrincipal principal: AuthPrincipal,
    ): InsightSettingsResponse = manageInsightSettingsUseCase.getOrDefault(principal.userId)

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    fun upsert(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @Valid @RequestBody request: InsightSettingsUpsertRequest,
    ): InsightSettingsResponse {
        val enabled =
            request.enabled
                ?: throw IllegalArgumentException("enabled는 필수입니다.")
        return manageInsightSettingsUseCase.upsert(principal.userId, enabled)
    }
}
