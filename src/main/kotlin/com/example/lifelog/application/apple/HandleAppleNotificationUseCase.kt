package com.example.lifelog.application.apple

import com.example.lifelog.domain.auth.AppleRefreshTokenRepository
import com.example.lifelog.domain.auth.OAuthAccountRepository
import com.example.lifelog.domain.auth.OAuthProvider
import com.example.lifelog.domain.auth.RefreshTokenRepository
import com.example.lifelog.domain.user.UserRepository
import com.example.lifelog.infrastructure.external.oauth.AppleJwtVerifier
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 애플 서버-to-서버 알림 처리 Use Case
 * 한국 기반 개발자는 2026년 1월 1일부터 필수
 */
@Service
class HandleAppleNotificationUseCase(
    private val appleJwtVerifier: AppleJwtVerifier,
    private val objectMapper: ObjectMapper,
    private val oauthAccountRepository: OAuthAccountRepository,
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val appleRefreshTokenRepository: AppleRefreshTokenRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 애플 서버-to-서버 알림 처리
     * @param jwtPayload JWT 토큰 (애플이 보내는 알림)
     */
    @Transactional
    fun execute(jwtPayload: String) {
        try {
            val claims = appleJwtVerifier.verifyAndParse(jwtPayload)
            val events = claims["events"] as? Map<*, *> ?: return

            // 이벤트 타입 확인
            val eventType = events.keys.firstOrNull()?.toString() ?: return
            val eventData = events[eventType] as? Map<*, *> ?: return

            log.info("[APPLE_NOTIFICATION] Received event. type={}, data={}", eventType, eventData)

            when (eventType) {
                "email-disabled" -> {
                    // 이메일 전달 비활성화 (계정 삭제와는 무관)
                    log.info("[APPLE_NOTIFICATION] Email forwarding disabled")
                }
                "email-enabled" -> {
                    // 이메일 전달 활성화
                    log.info("[APPLE_NOTIFICATION] Email forwarding enabled")
                }
                "consent-withdrawn" -> {
                    // 사용자가 Sign in with Apple 동의 철회 (계정 삭제)
                    handleConsentWithdrawn(eventData)
                }
                "account-delete" -> {
                    // 영구 Apple 계정 삭제
                    handleAccountDelete(eventData)
                }
                else -> {
                    log.warn("[APPLE_NOTIFICATION] Unknown event type: {}", eventType)
                }
            }
        } catch (e: Exception) {
            log.error("[APPLE_NOTIFICATION] Failed to process notification", e)
            throw e
        }
    }

    private fun handleConsentWithdrawn(eventData: Map<*, *>) {
        val sub = eventData["sub"]?.toString() ?: return
        log.info("[APPLE_NOTIFICATION] Consent withdrawn for sub={}", sub)

        // 해당 애플 계정으로 가입한 사용자 찾기
        val oauthAccount =
            oauthAccountRepository.findByProviderAndProviderUserId(OAuthProvider.APPLE, sub)
                ?: run {
                    log.warn("[APPLE_NOTIFICATION] OAuth account not found for sub={}", sub)
                    return
                }

        val userId = oauthAccount.userId
        val user =
            userRepository.findById(userId) ?: run {
                log.warn("[APPLE_NOTIFICATION] User not found for userId={}", userId)
                return
            }

        if (user.isDeleted()) {
            log.info("[APPLE_NOTIFICATION] User already deleted. userId={}", userId)
            return
        }

        // 계정 삭제 처리
        log.info("[APPLE_NOTIFICATION] Deleting user account. userId={}, sub={}", userId, sub)
        user.delete()
        userRepository.save(user)

        // 관련 데이터 삭제
        refreshTokenRepository.deleteAllByUserId(userId)
        oauthAccountRepository.deleteAllByUserId(userId)
        appleRefreshTokenRepository.deleteByUserId(userId)
    }

    private fun handleAccountDelete(eventData: Map<*, *>) {
        val sub = eventData["sub"]?.toString() ?: return
        log.info("[APPLE_NOTIFICATION] Account permanently deleted for sub={}", sub)

        // 영구 Apple 계정 삭제 처리 (consent-withdrawn과 동일하게 처리)
        handleConsentWithdrawn(eventData)
    }
}
