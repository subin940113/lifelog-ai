package com.example.lifelog.application.push

import com.example.lifelog.common.exception.ErrorCode
import com.example.lifelog.common.exception.ValidationException
import com.example.lifelog.domain.push.PushToken
import com.example.lifelog.domain.push.PushTokenRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * 푸시 토큰 등록/업데이트 Use Case
 */
@Service
class UpsertPushTokenUseCase(
    private val pushTokenRepository: PushTokenRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun execute(
        userId: Long,
        token: String,
        platform: String?,
    ): PushToken {
        val trimmedToken = token.trim()
        if (trimmedToken.isEmpty()) {
            throw ValidationException(ErrorCode.VALIDATION_BLANK_TOKEN)
        }

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
}
