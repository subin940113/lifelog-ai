package com.example.lifelog.push.api

import com.example.lifelog.auth.security.AuthPrincipal
import com.example.lifelog.push.PushTokenService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/push")
class PushTokenController(
    private val pushTokenService: PushTokenService,
) {
    @PostMapping("/token")
    fun register(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @RequestBody req: RegisterPushTokenRequest,
    ): ResponseEntity<RegisterPushTokenResponse> {
        val userId = principal.userId
        val token = req.token?.trim().orEmpty()
        if (token.isEmpty()) return ResponseEntity.badRequest().build()

        pushTokenService.upsert(
            userId = userId,
            token = token,
            platform = req.platform,
        )
        return ResponseEntity.ok(RegisterPushTokenResponse(ok = true))
    }

    @DeleteMapping("/token")
    fun delete(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @RequestParam token: String?,
    ): ResponseEntity<RegisterPushTokenResponse> {
        val userId = principal.userId
        val t = token?.trim().orEmpty()
        if (t.isEmpty()) return ResponseEntity.badRequest().build()

        pushTokenService.deleteToken(userId, t)
        return ResponseEntity.ok(RegisterPushTokenResponse(ok = true))
    }
}
