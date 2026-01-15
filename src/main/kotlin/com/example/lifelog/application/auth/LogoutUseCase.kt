package com.example.lifelog.application.auth

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 로그아웃 Use Case
 */
@Service
class LogoutUseCase(
    private val refreshTokenService: RefreshTokenService,
) {
    @Transactional
    fun logout(
        refreshToken: String,
        allDevices: Boolean = false,
    ) {
        if (allDevices) {
            val userId = refreshTokenService.requireValid(refreshToken).userId
            refreshTokenService.revokeAllForUser(userId)
        } else {
            refreshTokenService.revoke(refreshToken)
        }
    }
}
