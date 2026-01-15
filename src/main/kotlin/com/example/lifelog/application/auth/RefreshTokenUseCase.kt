package com.example.lifelog.application.auth

import com.example.lifelog.infrastructure.security.JwtProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 리프레시 토큰 갱신 Use Case
 */
@Service
class RefreshTokenUseCase(
    private val refreshTokenManagementUseCase: RefreshTokenManagementUseCase,
    private val jwtProvider: JwtProvider,
) {
    @Transactional
    fun execute(request: TokenRefreshRequest): TokenRefreshResponse {
        val rotation = refreshTokenManagementUseCase.rotate(request.refreshToken)
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
