package com.example.lifelog.presentation.api.auth

import com.example.lifelog.application.auth.AuthLoginResult
import com.example.lifelog.application.auth.LoginUseCase
import com.example.lifelog.application.auth.LogoutUseCase
import com.example.lifelog.application.auth.RefreshTokenUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 인증 API Controller
 */
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val loginUseCase: LoginUseCase,
    private val refreshTokenUseCase: RefreshTokenUseCase,
    private val logoutUseCase: LogoutUseCase,
) {
    @PostMapping("/oauth/google")
    fun google(
        @RequestBody request: GoogleLoginRequest,
    ): AuthLoginResult = loginUseCase.loginGoogle(request.idToken)

    @PostMapping("/oauth/kakao")
    fun loginKakao(
        @RequestBody request: OAuthAccessTokenLoginRequest,
    ): AuthLoginResult = loginUseCase.loginKakao(request.accessToken)

    @PostMapping("/oauth/naver")
    fun loginNaver(
        @RequestBody request: OAuthAccessTokenLoginRequest,
    ): AuthLoginResult = loginUseCase.loginNaver(request.accessToken)

    @PostMapping("/refresh")
    fun refresh(
        @RequestBody request: com.example.lifelog.application.auth.TokenRefreshRequest,
    ): ResponseEntity<com.example.lifelog.application.auth.TokenRefreshResponse> = ResponseEntity.ok(refreshTokenUseCase.execute(request))

    @PostMapping("/logout")
    fun logout(
        @RequestBody request: LogoutRequest,
    ): ResponseEntity<Unit> {
        logoutUseCase.logout(request.refreshToken, request.allDevices)
        return ResponseEntity.noContent().build()
    }
}
