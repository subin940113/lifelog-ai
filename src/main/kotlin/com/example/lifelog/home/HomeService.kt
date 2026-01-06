package com.example.lifelog.home

import com.example.lifelog.insight.AiInsightRepository
import com.example.lifelog.insight.InsightPeriod
import com.example.lifelog.log.raw.RawLogRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class HomeService(
    private val rawLogRepository: RawLogRepository,
    private val aiInsightRepository: AiInsightRepository,
) {
    private val zone: ZoneId = ZoneId.of("Asia/Seoul")
    private val dateFmt: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val mdFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("M/d")

    fun getHome(
        userId: Long,
        period: String,
        limitLogs: Int,
        limitInsights: Int,
    ): HomeResponse {
        val today: LocalDate = LocalDate.now(zone)
        val todayStart: Instant = today.atStartOfDay(zone).toInstant()

        val signalCount =
            rawLogRepository.countByUserIdAndCreatedAtGreaterThanEqual(userId, todayStart).toInt()

        val safeLimitLogs = limitLogs.coerceIn(1, 20)
        val safeLimitInsights = limitInsights.coerceIn(0, 20)

        val recentLogs = loadRecentLogs(userId, safeLimitLogs, today)
        val lastTimeLabel = recentLogs.firstOrNull()?.timeLabel ?: "—"

        val headline =
            when {
                signalCount <= 1 -> "아직 해석할 신호가 충분하지 않아요"
                else -> "새로운 패턴 후보가 감지됐어요"
            }

        val top =
            TopInsightDto(
                date = today.format(dateFmt),
                headline = headline,
                signalCount = signalCount,
                axes = emptyList(),
                lastTimeLabel = lastTimeLabel,
            )

        // period는 현재 HomeResponse에 쓰는 용도 외에, 조회 필터로는 사용하지 않음(Heuristic 단계)
        resolvePeriod(period, today)

        val insights: List<AiInsightDto> =
            if (safeLimitInsights <= 0) {
                emptyList()
            } else {
                val pageable = PageRequest.of(0, safeLimitInsights, Sort.by(Sort.Direction.DESC, "createdAt"))
                aiInsightRepository
                    .findLatestByUserId(userId, pageable)
                    .map { a ->
                        AiInsightDto(
                            kind = a.kind.name,
                            title = a.title,
                            body = a.body,
                            evidence = a.evidence,
                        )
                    }
            }

        val topInsight =
            if (insights.isNotEmpty()) top.copy(headline = insights.first().title) else top

        return HomeResponse(
            topInsight = topInsight,
            insights = insights,
            recentLogs = recentLogs,
        )
    }

    private fun resolvePeriod(
        period: String,
        today: LocalDate,
    ): Triple<InsightPeriod, LocalDate, LocalDate> =
        when (period.lowercase()) {
            "week" -> Triple(InsightPeriod.WEEK, today.minusDays(6), today)
            "month" -> Triple(InsightPeriod.MONTH, today.minusDays(29), today)
            else -> Triple(InsightPeriod.DAY, today, today)
        }

    private fun loadRecentLogs(
        userId: Long,
        limit: Int,
        today: LocalDate,
    ): List<RecentLogDto> {
        // ✅ 지금 너가 RawLogRepository를 커서 기반으로 바꾼 상태에 맞춰서
        // findFirstPage(userId, pageable)로 최신 목록을 가져온다고 가정
        val pageable = PageRequest.of(0, limit)
        val logs = rawLogRepository.findFirstPage(userId = userId, pageable = pageable)

        return logs.map { l ->
            val created = l.createdAt.atZone(zone)
            val d = created.toLocalDate()

            val timeLabel =
                when {
                    d.isEqual(today) -> created.format(timeFmt)
                    d.isEqual(today.minusDays(1)) -> "어제"
                    else -> d.format(mdFmt)
                }

            RecentLogDto(
                logId = l.id,
                timeLabel = timeLabel,
                preview = toPreview(l.content),
            )
        }
    }

    private fun toPreview(content: String): String {
        val singleLine = content.replace("\n", " ").trim()
        val max = 90
        return if (singleLine.length <= max) singleLine else singleLine.substring(0, max).trimEnd() + "…"
    }
}
