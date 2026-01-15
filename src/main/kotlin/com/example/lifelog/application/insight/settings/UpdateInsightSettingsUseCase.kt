package com.example.lifelog.application.insight.settings

import com.example.lifelog.domain.insight.settings.InsightSettings
import com.example.lifelog.domain.insight.settings.InsightSettingsRepository
import com.example.lifelog.presentation.api.insight.InsightSettingsResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * 인사이트 설정 업데이트 Use Case
 */
@Service
class UpdateInsightSettingsUseCase(
    private val settingsRepository: InsightSettingsRepository,
) {
    @Transactional
    fun execute(
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
