package com.example.lifelog.insight.pipeline

import com.example.lifelog.log.raw.RawLog
import java.time.Instant

data class InsightContext(
    val userId: Long,
    val matchedKeyword: String?,
    val triggerLog: RawLog,
    val sourceLogId: Long,
    val logs: List<RawLog>,
    val recentInsights: List<RecentInsight> = emptyList(),
)

data class RecentInsight(
    val id: Long,
    val kind: String,
    val title: String,
    val body: String,
    val createdAt: Instant,
)
