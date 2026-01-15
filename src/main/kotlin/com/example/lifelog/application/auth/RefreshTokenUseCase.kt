package com.example.lifelog.application.auth

import com.example.lifelog.infrastructure.security.JwtProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 리프레시 토큰 갱신 Use Case
 */
@Service
class RefreshTokenUseCase(
    private val refreshTokenService: RefreshTokenService,
    private val jwtProvider: JwtProvider,
) {
    @Transactional
    fun refresh(request: TokenRefreshRequest): TokenRefreshResponse {
        val rotation = refreshTokenService.rotate(request.refreshToken)
        val newAccess = jwtProvider.createAccessToken(rotation.userId)
        return TokenRefreshResponse(
            accessToken = newAccess,
            refreshToken = rotation.newRefreshToken,
        )
    }
}

/**
 * 토큰 갱신 요청
 */
data class TokenRefreshRequest(
    val refreshToken: String,
)

/**
 * 토큰 갱신 응답
 */
data class TokenRefreshResponse(
    val accessToken: String,
    val refreshToken: String,
)
