package com.example.lifelog.presentation.api.interest

import com.example.lifelog.application.interest.InterestResponse
import com.example.lifelog.application.interest.ManageInterestUseCase
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
 * 관심사 API Controller
 */
@RestController
@RequestMapping("/api/interests")
class InterestController(
    private val manageInterestUseCase: ManageInterestUseCase,
) {
    @GetMapping
    fun get(
        @AuthenticationPrincipal principal: AuthPrincipal,
    ): InterestResponse = manageInterestUseCase.getInterests(principal.userId)

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    fun addKeyword(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @Valid @RequestBody request: InterestKeywordRequest,
    ): InterestResponse {
        val keyword = request.keyword ?: throw IllegalArgumentException("keyword는 필수입니다.")
        return manageInterestUseCase.addKeyword(principal.userId, keyword)
    }

    // 기존 클라 변경 최소화를 위해 POST /remove 유지
    @PostMapping("/remove")
    @ResponseStatus(HttpStatus.OK)
    fun removeKeyword(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @Valid @RequestBody request: InterestKeywordRequest,
    ): InterestResponse {
        val keyword = request.keyword ?: throw IllegalArgumentException("keyword는 필수입니다.")
        return manageInterestUseCase.removeKeyword(principal.userId, keyword)
    }
}
