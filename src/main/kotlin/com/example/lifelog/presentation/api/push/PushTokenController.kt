package com.example.lifelog.presentation.api.push

import com.example.lifelog.application.push.ManagePushTokenUseCase
import com.example.lifelog.infrastructure.security.AuthPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * 푸시 토큰 API Controller
 */
@RestController
@RequestMapping("/api/push")
class PushTokenController(
    private val managePushTokenUseCase: ManagePushTokenUseCase,
) {
    @PostMapping("/token")
    fun register(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @RequestBody request: RegisterPushTokenRequest,
    ): ResponseEntity<RegisterPushTokenResponse> {
        val userId = principal.userId
        val token = request.token?.trim().orEmpty()
        if (token.isEmpty()) return ResponseEntity.badRequest().build()

        managePushTokenUseCase.upsert(
            userId = userId,
            token = token,
            platform = request.platform,
        )
        return ResponseEntity.ok(RegisterPushTokenResponse(ok = true))
    }

    @DeleteMapping("/token")
    fun delete(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @RequestParam token: String?,
    ): ResponseEntity<RegisterPushTokenResponse> {
        val userId = principal.userId
        val trimmedToken = token?.trim().orEmpty()
        if (trimmedToken.isEmpty()) return ResponseEntity.badRequest().build()

        managePushTokenUseCase.deleteToken(userId, trimmedToken)
        return ResponseEntity.ok(RegisterPushTokenResponse(ok = true))
    }
}

/**
 * 푸시 토큰 등록 요청 DTO
 */
data class RegisterPushTokenRequest(
    val token: String?,
    val platform: String? = null, // optional: "android" | "ios"
)

/**
 * 푸시 토큰 등록 응답 DTO
 */
data class RegisterPushTokenResponse(
    val ok: Boolean = true,
)
