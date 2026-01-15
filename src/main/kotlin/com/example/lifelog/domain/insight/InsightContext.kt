package com.example.lifelog.domain.insight

import com.example.lifelog.domain.log.RawLog
import java.time.Instant

/**
 * 인사이트 생성 컨텍스트
 */
data class InsightContext(
    val userId: Long,
    val matchedKeyword: String?,
    val triggerLog: RawLog,
    val sourceLogId: Long,
    val logs: List<RawLog>,
    val recentInsights: List<RecentInsight> = emptyList(),
)

/**
 * 최근 인사이트 정보
 */
data class RecentInsight(
    val id: Long,
    val kind: String,
    val title: String,
    val body: String,
    val createdAt: Instant,
)
