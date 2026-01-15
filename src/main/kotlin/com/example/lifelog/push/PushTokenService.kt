package com.example.lifelog.push

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class PushTokenService(
    private val repo: PushTokenRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun upsert(
        userId: Long,
        token: String,
        platform: String?,
    ): PushToken {
        val t = token.trim()
        require(t.isNotEmpty()) { "token is blank" }

        val existing = repo.findByUserIdAndToken(userId, t)
        val now = Instant.now()

        if (existing != null) {
            existing.enabled = true
            existing.platform = platform?.trim()?.takeIf { it.isNotEmpty() } ?: existing.platform
            existing.lastSeenAt = now
            log.info("[PushToken] updated userId={} tokenPrefix={}", userId, t.take(8))
            return existing // dirty-checking으로 save 불필요
        }

        val saved =
            repo.save(
                PushToken(
                    userId = userId,
                    token = t,
                    platform = platform?.trim()?.takeIf { it.isNotEmpty() },
                    enabled = true,
                    lastSeenAt = now,
                ),
            )
        log.info("[PushToken] inserted userId={} tokenPrefix={}", userId, t.take(8))
        return saved
    }

    @Transactional(readOnly = true)
    fun getEnabledTokens(userId: Long): List<String> = repo.findEnabledByUserId(userId).map { it.token }

    @Transactional
    fun disableToken(
        userId: Long,
        token: String,
    ) {
        val t = token.trim()
        val row = repo.findByUserIdAndToken(userId, t) ?: return
        if (row.enabled) {
            row.enabled = false
            log.info("[PushToken] disabled userId={} tokenPrefix={}", userId, t.take(8))
        }
    }

    @Transactional
    fun deleteToken(
        userId: Long,
        token: String,
    ): Boolean {
        val deleted = repo.deleteByUserIdAndToken(userId, token.trim())
        return deleted > 0
    }
}
