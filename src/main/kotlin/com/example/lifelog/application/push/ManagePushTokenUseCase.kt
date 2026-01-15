package com.example.lifelog.application.push

import com.example.lifelog.domain.push.PushToken
import com.example.lifelog.domain.push.PushTokenRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * 푸시 토큰 관리 Use Case
 */
@Service
class ManagePushTokenUseCase(
    private val pushTokenRepository: PushTokenRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun upsert(
        userId: Long,
        token: String,
        platform: String?,
    ): PushToken {
        val trimmedToken = token.trim()
        require(trimmedToken.isNotEmpty()) { "token is blank" }

        val existingToken = pushTokenRepository.findByUserIdAndToken(userId, trimmedToken)
        val now = Instant.now()

        if (existingToken != null) {
            existingToken.enable()
            existingToken.platform = platform?.trim()?.takeIf { it.isNotEmpty() } ?: existingToken.platform
            existingToken.updateLastSeen()
            log.info("[PushToken] updated userId={} tokenPrefix={}", userId, trimmedToken.take(8))
            return pushTokenRepository.save(existingToken)
        }

        val savedToken =
            pushTokenRepository.save(
                PushToken(
                    userId = userId,
                    token = trimmedToken,
                    platform = platform?.trim()?.takeIf { it.isNotEmpty() },
                    enabled = true,
                    lastSeenAt = now,
                ),
            )
        log.info("[PushToken] inserted userId={} tokenPrefix={}", userId, trimmedToken.take(8))
        return savedToken
    }

    @Transactional(readOnly = true)
    fun getEnabledTokens(userId: Long): List<String> =
        pushTokenRepository.findEnabledByUserId(userId).map { it.token }

    @Transactional
    fun disableToken(
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

    @Transactional
    fun deleteToken(
        userId: Long,
        token: String,
    ): Boolean {
        val deleted = pushTokenRepository.deleteByUserIdAndToken(userId, token.trim())
        return deleted > 0
    }
}
