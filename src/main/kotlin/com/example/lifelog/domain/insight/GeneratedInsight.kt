package com.example.lifelog.domain.insight

/**
 * 생성된 인사이트 도메인 모델
 */
data class GeneratedInsight(
    val kind: InsightKind,
    val title: String,
    val body: String,
    val evidence: String? = null,
    val keyword: String? = null,
)
