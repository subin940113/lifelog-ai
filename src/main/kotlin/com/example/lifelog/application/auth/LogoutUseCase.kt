package com.example.lifelog.application.auth

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 로그아웃 Use Case
 */
@Service
class LogoutUseCase(
    private val refreshTokenManagementUseCase: RefreshTokenManagementUseCase,
) {
    @Transactional
    fun logout(
        refreshToken: String,
        allDevices: Boolean = false,
    ) {
        if (allDevices) {
            val userId = refreshTokenManagementUseCase.requireValid(refreshToken).userId
            refreshTokenManagementUseCase.revokeAllForUser(userId)
        } else {
            refreshTokenManagementUseCase.revoke(refreshToken)
        }
    }
}
