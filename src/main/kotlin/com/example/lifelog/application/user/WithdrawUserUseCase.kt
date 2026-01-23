package com.example.lifelog.application.user

import com.example.lifelog.common.exception.ErrorCode
import com.example.lifelog.common.exception.NotFoundException
import com.example.lifelog.domain.auth.AppleRefreshTokenRepository
import com.example.lifelog.domain.auth.OAuthAccountRepository
import com.example.lifelog.domain.auth.OAuthProvider
import com.example.lifelog.domain.auth.RefreshTokenRepository
import com.example.lifelog.domain.user.UserRepository
import com.example.lifelog.infrastructure.external.oauth.AppleOAuthProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 사용자 계정 탈퇴 Use Case
 */
@Service
class WithdrawUserUseCase(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val oauthAccountRepository: OAuthAccountRepository,
    private val appleRefreshTokenRepository: AppleRefreshTokenRepository,
    private val appleOAuthProvider: AppleOAuthProvider,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun execute(userId: Long) {
        val user =
            userRepository.findById(userId)
                ?: throw NotFoundException(ErrorCode.NOT_FOUND_USER, "User not found: $userId")

        if (user.isDeleted()) return

        // 애플 계정인 경우 revoke 처리
        val oauthAccounts = oauthAccountRepository.findByUserId(userId)
        val appleAccount = oauthAccounts.firstOrNull { it.provider == OAuthProvider.APPLE }

        if (appleAccount != null) {
            val appleRefreshToken = appleRefreshTokenRepository.findByUserId(userId)
            if (appleRefreshToken != null) {
                try {
                    log.info("[WITHDRAW] Revoking Apple token for userId={}", userId)
                    appleOAuthProvider.revokeToken(appleRefreshToken.refreshToken, "refresh_token")
                } catch (e: Exception) {
                    log.warn("[WITHDRAW] Failed to revoke Apple token, but continuing with account deletion", e)
                    // 계정 삭제는 계속 진행
                }
            } else {
                log.warn("[WITHDRAW] Apple account found but refresh_token is null. userId={}", userId)
            }
        }

        user.delete()
        userRepository.save(user)

        // 관련 데이터 삭제
        refreshTokenRepository.deleteAllByUserId(userId)
        oauthAccountRepository.deleteAllByUserId(userId)
        appleRefreshTokenRepository.deleteByUserId(userId)
    }
}
