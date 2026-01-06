package com.example.lifelog.insight.settings

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class InsightSettingsService(
    private val repo: InsightSettingsRepository,
) {
    @Transactional(readOnly = true)
    fun getOrDefault(userId: Long): InsightSettingsResponse {
        val s = repo.findByUserId(userId)
        return InsightSettingsResponse(enabled = s?.enabled ?: false)
    }

    @Transactional
    fun upsert(
        userId: Long,
        enabled: Boolean,
    ): InsightSettingsResponse {
        val now = Instant.now()
        val existing = repo.findByUserId(userId)

        if (existing != null) {
            existing.enabled = enabled
            existing.updatedAt = now
            repo.save(existing)
            return InsightSettingsResponse(enabled = existing.enabled)
        }

        repo.save(
            InsightSettings(
                userId = userId,
                enabled = enabled,
                updatedAt = now,
            ),
        )
        return InsightSettingsResponse(enabled = enabled)
    }
}
