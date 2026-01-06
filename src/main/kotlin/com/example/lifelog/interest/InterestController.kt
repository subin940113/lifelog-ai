package com.example.lifelog.interest

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
@RequestMapping("/api/interests")
class InterestController(
    private val service: InterestService,
) {
    @GetMapping
    fun get(
        @AuthenticationPrincipal principal: AuthPrincipal,
    ): InterestStateResponse = service.getOrDefault(principal.userId)

    @PostMapping("/enabled")
    @ResponseStatus(HttpStatus.OK)
    fun setEnabled(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @Valid @RequestBody req: InterestEnabledRequest,
    ): InterestStateResponse {
        val enabled = req.enabled ?: throw IllegalArgumentException("enabled는 필수입니다.")
        return service.setEnabled(principal.userId, enabled)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    fun addKeyword(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @Valid @RequestBody req: InterestKeywordRequest,
    ): InterestStateResponse {
        val keyword = req.keyword ?: throw IllegalArgumentException("keyword는 필수입니다.")
        return service.addKeyword(principal.userId, keyword)
    }

    @PostMapping("/remove")
    @ResponseStatus(HttpStatus.OK)
    fun removeKeyword(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @Valid @RequestBody req: InterestKeywordRequest,
    ): InterestStateResponse {
        val keyword = req.keyword ?: throw IllegalArgumentException("keyword는 필수입니다.")
        return service.removeKeyword(principal.userId, keyword)
    }
}
