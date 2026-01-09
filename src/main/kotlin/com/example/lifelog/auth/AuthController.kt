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
    ): AuthLoginResult = authService.loginGoogle(req.idToken)

    @PostMapping("/oauth/kakao")
    fun loginKakao(
        @RequestBody req: OAuthAccessTokenLoginRequest,
    ): AuthLoginResult = authService.loginKakao(req.accessToken)

    @PostMapping("/oauth/naver")
    fun loginNaver(
        @RequestBody req: OAuthAccessTokenLoginRequest,
    ): AuthLoginResult = authService.loginNaver(req.accessToken)

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
