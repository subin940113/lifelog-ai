package com.example.lifelog.domain.insight

/**
 * 인사이트 트리거 결정 결과
 */
data class InsightTriggerDecision(
    val shouldRun: Boolean,
    val reason: String? = null,
    val matchedKeyword: String? = null,
)
