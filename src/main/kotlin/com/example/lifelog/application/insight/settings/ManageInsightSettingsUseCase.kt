package com.example.lifelog.application.insight.settings

import com.example.lifelog.domain.insight.settings.InsightSettings
import com.example.lifelog.domain.insight.settings.InsightSettingsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * 인사이트 설정 관리 Use Case
 */
@Service
class ManageInsightSettingsUseCase(
    private val settingsRepository: InsightSettingsRepository,
) {
    @Transactional(readOnly = true)
    fun getOrDefault(userId: Long): InsightSettingsResponse {
        val settings = settingsRepository.findByUserId(userId)
        return InsightSettingsResponse(enabled = settings?.enabled ?: false)
    }

    @Transactional
    fun upsert(
        userId: Long,
        enabled: Boolean,
    ): InsightSettingsResponse {
        val now = Instant.now()
        val existing = settingsRepository.findByUserId(userId)

        if (existing != null) {
            existing.enabled = enabled
            existing.updatedAt = now
            settingsRepository.save(existing)
            return InsightSettingsResponse(enabled = existing.enabled)
        }

        settingsRepository.save(
            InsightSettings(
                userId = userId,
                enabled = enabled,
                updatedAt = now,
            ),
        )
        return InsightSettingsResponse(enabled = enabled)
    }
}

/**
 * 인사이트 설정 응답 DTO
 */
data class InsightSettingsResponse(
    val enabled: Boolean,
)
