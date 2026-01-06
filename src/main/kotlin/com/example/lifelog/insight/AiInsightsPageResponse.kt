package com.example.lifelog.insight

data class AiInsightsPageResponse(
    val insights: List<AiInsightListItem>,
    val nextCursor: String?,
)
