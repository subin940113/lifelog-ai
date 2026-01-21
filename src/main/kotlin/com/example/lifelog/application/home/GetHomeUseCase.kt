package com.example.lifelog.application.home

import com.example.lifelog.common.time.TimeZoneConfig
import com.example.lifelog.domain.insight.InsightRepository
import com.example.lifelog.domain.log.LogRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 홈 화면 조회 Use Case
 */
@Service
class GetHomeUseCase(
    private val logRepository: LogRepository,
    private val insightRepository: InsightRepository,
    private val timeZoneConfig: TimeZoneConfig,
) {
    private val zoneId = timeZoneConfig.defaultZoneId
    private val dateFmt: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val mdFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("M/d")

    @Transactional(readOnly = true)
    fun execute(
        userId: Long,
        period: String,
        limitLogs: Int,
        limitInsights: Int,
    ): HomeResponse {
        val today: LocalDate = LocalDate.now(zoneId)
        val todayStart: Instant = today.atStartOfDay(zoneId).toInstant()

        val signalCount = logRepository.countByUserIdAndCreatedAtGreaterThanEqual(userId, todayStart).toInt()

        val safeLimitLogs = limitLogs.coerceIn(1, 20)
        val safeLimitInsights = limitInsights.coerceIn(0, 20)

        val recentLogs = loadRecentLogs(userId, safeLimitLogs, today)
        val lastTimeLabel = recentLogs.firstOrNull()?.timeLabel ?: "—"

        // 앱에서 설정
        val headline = ""

        val topInsight =
            TopInsightResponse.from(
                date = today.format(dateFmt),
                headline = headline,
                signalCount = signalCount,
                axes = emptyList(),
                lastTimeLabel = lastTimeLabel,
            )

        val insights: List<InsightResponse> =
            if (safeLimitInsights <= 0) {
                emptyList()
            } else {
                val pageable = PageRequest.of(0, safeLimitInsights, Sort.by(Sort.Direction.DESC, "createdAt"))
                insightRepository
                    .findLatestByUserId(userId, pageable)
                    .map { InsightResponse.from(it) }
            }

        val updatedTopInsight =
            if (insights.isNotEmpty()) topInsight.copy(headline = insights.first().title) else topInsight

        return HomeResponse(
            topInsight = updatedTopInsight,
            insights = insights,
            recentLogs = recentLogs,
        )
    }

    private fun loadRecentLogs(
        userId: Long,
        limit: Int,
        today: LocalDate,
    ): List<RecentLogResponse> {
        val pageable = PageRequest.of(0, limit)
        val logs = logRepository.findLatestByUserId(userId, pageable)

        return logs.map { RecentLogResponse.from(it, zoneId, today, timeFmt, mdFmt) }
    }
}

/**
 * 홈 응답 DTO
 */
data class HomeResponse(
    val topInsight: TopInsightResponse,
    val insights: List<InsightResponse>,
    val recentLogs: List<RecentLogResponse>,
)

data class TopInsightResponse(
    val date: String,
    val headline: String,
    val signalCount: Int,
    val axes: List<Any>,
    val lastTimeLabel: String,
) {
    companion object {
        fun from(
            date: String,
            headline: String,
            signalCount: Int,
            axes: List<Any>,
            lastTimeLabel: String,
        ): TopInsightResponse =
            TopInsightResponse(
                date = date,
                headline = headline,
                signalCount = signalCount,
                axes = axes,
                lastTimeLabel = lastTimeLabel,
            )
    }
}

data class InsightResponse(
    val id: Long,
    val kind: String,
    val title: String,
    val body: String,
    val evidence: String?,
) {
    companion object {
        fun from(insight: com.example.lifelog.domain.insight.Insight): InsightResponse =
            InsightResponse(
                id = insight.id,
                kind = insight.kind.name,
                title = insight.title,
                body = insight.body,
                evidence = insight.evidence,
            )
    }
}

data class RecentLogResponse(
    val logId: Long,
    val timeLabel: String,
    val preview: String,
) {
    companion object {
        fun from(
            log: com.example.lifelog.domain.log.RawLog,
            zoneId: ZoneId,
            today: LocalDate,
            timeFmt: DateTimeFormatter,
            mdFmt: DateTimeFormatter,
        ): RecentLogResponse {
            val created = log.createdAt.atZone(zoneId)
            val d = created.toLocalDate()

            val timeLabel =
                when {
                    d.isEqual(today) -> created.format(timeFmt)
                    d.isEqual(today.minusDays(1)) -> "어제"
                    else -> d.format(mdFmt)
                }

            return RecentLogResponse(
                logId = log.id,
                timeLabel = timeLabel,
                preview = log.preview(90),
            )
        }
    }
}
