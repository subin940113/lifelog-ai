package com.example.lifelog.application.home

import com.example.lifelog.common.time.DateTimeFormatters
import com.example.lifelog.common.time.TimeZoneConfig
import com.example.lifelog.domain.log.LogRepository
import com.example.lifelog.domain.insight.InsightRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
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

        val headline =
            when {
                signalCount <= 1 -> "아직 해석할 신호가 충분하지 않아요"
                else -> "새로운 패턴 후보가 감지됐어요"
            }

        val topInsight =
            TopInsightDto(
                date = today.format(dateFmt),
                headline = headline,
                signalCount = signalCount,
                axes = emptyList(),
                lastTimeLabel = lastTimeLabel,
            )

        val insights: List<InsightDto> =
            if (safeLimitInsights <= 0) {
                emptyList()
            } else {
                val pageable = PageRequest.of(0, safeLimitInsights, Sort.by(Sort.Direction.DESC, "createdAt"))
                insightRepository
                    .findLatestByUserId(userId, pageable)
                    .map { insight ->
                        InsightDto(
                            id = insight.id,
                            kind = insight.kind.name,
                            title = insight.title,
                            body = insight.body,
                            evidence = insight.evidence,
                        )
                    }
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
    ): List<RecentLogDto> {
        val pageable = PageRequest.of(0, limit)
        val logs = logRepository.findLatestByUserId(userId, pageable)

        return logs.map { log ->
            val created = log.createdAt.atZone(zoneId)
            val d = created.toLocalDate()

            val timeLabel =
                when {
                    d.isEqual(today) -> created.format(timeFmt)
                    d.isEqual(today.minusDays(1)) -> "어제"
                    else -> d.format(mdFmt)
                }

            RecentLogDto(
                logId = log.id,
                timeLabel = timeLabel,
                preview = log.preview(90),
            )
        }
    }
}

/**
 * 홈 응답 DTO
 */
data class HomeResponse(
    val topInsight: TopInsightDto,
    val insights: List<InsightDto>,
    val recentLogs: List<RecentLogDto>,
)

data class TopInsightDto(
    val date: String,
    val headline: String,
    val signalCount: Int,
    val axes: List<Any>,
    val lastTimeLabel: String,
)

data class InsightDto(
    val id: Long,
    val kind: String,
    val title: String,
    val body: String,
    val evidence: String?,
)

data class RecentLogDto(
    val logId: Long,
    val timeLabel: String,
    val preview: String,
)
