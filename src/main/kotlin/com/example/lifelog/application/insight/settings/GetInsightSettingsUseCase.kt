package com.example.lifelog.application.insight.settings

import com.example.lifelog.domain.insight.settings.InsightSettingsRepository
import com.example.lifelog.presentation.api.insight.InsightSettingsResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 인사이트 설정 조회 Use Case
 */
@Service
class GetInsightSettingsUseCase(
    private val settingsRepository: InsightSettingsRepository,
) {
    @Transactional(readOnly = true)
    fun execute(userId: Long): InsightSettingsResponse {
        val settings = settingsRepository.findByUserId(userId)
        return InsightSettingsResponse(enabled = settings?.enabled ?: false)
    }
}
