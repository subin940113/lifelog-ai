package com.example.lifelog.auth

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/oauth/google")
    fun google(
        @RequestBody req: GoogleLoginRequest,
    ): GoogleLoginResult = authService.loginGoogle(req.idToken)

    @PostMapping("/refresh")
    fun refresh(
        @RequestBody req: TokenRefreshRequest,
    ): ResponseEntity<TokenRefreshResponse> = ResponseEntity.ok(authService.refresh(req))

    @PostMapping("/logout")
    fun logout(
        @RequestBody req: LogoutRequest,
    ): ResponseEntity<Unit> {
        authService.logout(req.refreshToken, req.allDevices)
        return ResponseEntity.noContent().build()
    }
}
