package com.example.lifelog.insight

import com.example.lifelog.insight.settings.InsightSettingsService
import com.example.lifelog.interest.InterestService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InsightGateService(
    private val insightSettingsService: InsightSettingsService,
    private val interestService: InterestService,
) {
    @Transactional(readOnly = true)
    fun getGate(userId: Long): InsightGateState {
        val settingsEnabled = insightSettingsService.getOrDefault(userId).enabled
        val interest = interestService.getOrDefault(userId)

        // 관심사 키워드 정규화
        val keywords =
            interest.keywords
                .map { it.trim() }
                .filter { it.isNotEmpty() }

        // 정책:
        // 1) 인사이트 설정 ON
        // 2) 관심사 키워드 최소 1개 이상
        val enabled = settingsEnabled && keywords.isNotEmpty()

        return InsightGateState(
            enabled = enabled,
            keywords = keywords,
        )
    }
}
