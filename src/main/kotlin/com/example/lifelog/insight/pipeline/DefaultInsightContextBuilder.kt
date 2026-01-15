package com.example.lifelog.insight.pipeline

import com.example.lifelog.insight.AiInsightRepository
import com.example.lifelog.log.raw.RawLog
import com.example.lifelog.log.raw.RawLogRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Duration
import java.time.Instant

@Component
class DefaultInsightContextBuilder(
    private val rawLogRepository: RawLogRepository,
    private val insightRepository: AiInsightRepository,
    private val props: InsightPolicyProperties,
    private val clock: Clock = Clock.systemUTC(),
) : InsightContextBuilder {
    override fun build(
        userId: Long,
        rawLog: RawLog,
        matchedKeyword: String?,
    ): InsightContext {
        // 1) 최근 로그 윈도우 구성 (trigger 포함)
        val logLimit = props.candidateWindowSize.coerceIn(5, 200)

        val recentLogs =
            rawLogRepository.findLatestByUserId(
                userId = userId,
                pageable = PageRequest.of(0, logLimit),
            )

        // triggerLog가 recentLogs에 없을 수도 있으므로, 포함 보장
        val logs =
            (listOf(rawLog) + recentLogs)
                .distinctBy { it.id } // RawLog에 id가 있다고 가정. 없다면 content+createdAt 등으로 대체
                .sortedByDescending { it.createdAt ?: Instant.EPOCH } // most recent first

        // 2) 최근 인사이트(중복 방지 입력) 구성
        val hours = props.recentInsightsHours.coerceIn(6, 168) // 6h~7d
        val since = Instant.now(clock).minus(Duration.ofHours(hours.toLong()))

        val insights =
            insightRepository
                .findLatestByUserId(
                    userId = userId,
                    pageable = PageRequest.of(0, props.maxRecentInsights.coerceIn(1, 10)),
                ).asSequence()
                .filter { it.createdAt.isAfter(since) }
                .map {
                    RecentInsight(
                        id = it.id,
                        kind = it.kind.name,
                        title = it.title,
                        body = it.body,
                        createdAt = it.createdAt,
                    )
                }.toList()

        return InsightContext(
            userId = userId,
            matchedKeyword = matchedKeyword,
            triggerLog = rawLog,
            sourceLogId = rawLog.id,
            logs = logs,
            recentInsights = insights,
        )
    }
}
