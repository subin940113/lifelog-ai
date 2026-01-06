package com.example.lifelog.insight.settings

import com.example.lifelog.auth.security.AuthPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/insights/settings")
class InsightSettingsController(
    private val service: InsightSettingsService,
) {
    @GetMapping
    fun get(
        @AuthenticationPrincipal principal: AuthPrincipal,
    ): InsightSettingsResponse = service.getOrDefault(principal.userId)

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    fun upsert(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @Valid @RequestBody req: InsightSettingsUpsertRequest,
    ): InsightSettingsResponse {
        val enabled =
            req.enabled
                ?: throw IllegalArgumentException("enabled는 필수입니다.")
        return service.upsert(principal.userId, enabled)
    }
}
