package com.example.lifelog.application.insight

import com.example.lifelog.application.insight.settings.ManageInsightSettingsUseCase
import com.example.lifelog.application.interest.ManageInterestUseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 인사이트 게이트 상태 조회 Use Case
 */
@Service
class GetInsightGateUseCase(
    private val manageInsightSettingsUseCase: ManageInsightSettingsUseCase,
    private val manageInterestUseCase: ManageInterestUseCase,
) {
    @Transactional(readOnly = true)
    fun execute(userId: Long): InsightGateState {
        val settingsEnabled = manageInsightSettingsUseCase.getOrDefault(userId).enabled
        val interests = manageInterestUseCase.getInterests(userId)

        // 관심사 키워드 정규화
        val keywords =
            interests.keywords
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

/**
 * 인사이트 게이트 상태
 */
data class InsightGateState(
    val enabled: Boolean,
    val keywords: List<String>,
)
