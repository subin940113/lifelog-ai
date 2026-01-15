package com.example.lifelog.home

data class HomeResponse(
    val topInsight: TopInsightDto,
    val insights: List<AiInsightDto>,
    val recentLogs: List<RecentLogDto>,
)

data class TopInsightDto(
    val date: String, // yyyy-MM-dd
    val headline: String, // server-driven
    val signalCount: Int, // server-driven
    val axes: List<String>, // server-driven (MVP에서는 empty)
    val lastTimeLabel: String, // e.g. 13:20 / 어제 / M/d / —
)

data class RecentLogDto(
    val logId: Long,
    val timeLabel: String,
    val preview: String,
)

data class AiInsightDto(
    val id: Long,
    val kind: String,
    val title: String,
    val body: String,
    val evidence: String? = null,
)
