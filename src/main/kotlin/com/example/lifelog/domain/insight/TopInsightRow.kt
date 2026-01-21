package com.example.lifelog.domain.insight

data class TopInsightRow(
    val insightId: Long,
    val title: String,
    val body: String,
    val evidence: String?,
)
