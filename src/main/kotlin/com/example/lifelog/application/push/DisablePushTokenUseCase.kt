package com.example.lifelog.application.push

import com.example.lifelog.domain.push.PushTokenRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 푸시 토큰 비활성화 Use Case
 */
@Service
class DisablePushTokenUseCase(
    private val pushTokenRepository: PushTokenRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun execute(
        userId: Long,
        token: String,
    ) {
        val trimmedToken = token.trim()
        val pushToken = pushTokenRepository.findByUserIdAndToken(userId, trimmedToken) ?: return
        if (pushToken.enabled) {
            pushToken.disable()
            pushTokenRepository.save(pushToken)
            log.info("[PushToken] disabled userId={} tokenPrefix={}", userId, trimmedToken.take(8))
        }
    }
}
