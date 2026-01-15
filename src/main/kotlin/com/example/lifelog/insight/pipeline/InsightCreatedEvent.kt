package com.example.lifelog.insight.pipeline

data class InsightCreatedEvent(
    val userId: Long,
    val insightId: Long,
    val title: String,
)
