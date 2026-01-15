package com.example.lifelog.domain.insight

/**
 * 인사이트 생성 이벤트
 */
data class InsightCreatedEvent(
    val userId: Long,
    val insightId: Long,
    val title: String,
)
