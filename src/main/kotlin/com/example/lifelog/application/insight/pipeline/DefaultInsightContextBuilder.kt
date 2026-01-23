package com.example.lifelog.application.insight.pipeline

import com.example.lifelog.domain.insight.InsightContext
import com.example.lifelog.domain.insight.InsightRepository
import com.example.lifelog.domain.insight.RecentInsight
import com.example.lifelog.domain.log.LogRepository
import com.example.lifelog.domain.log.RawLog
import com.example.lifelog.infrastructure.config.InsightPolicyProperties
import com.example.lifelog.infrastructure.security.LogEncryption
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Duration
import java.time.Instant

@Component
class DefaultInsightContextBuilder(
    private val logRepository: LogRepository,
    private val insightRepository: InsightRepository,
    private val properties: InsightPolicyProperties,
    private val logEncryption: LogEncryption,
    private val clock: Clock = Clock.systemUTC(),
) : InsightContextBuilder {
    override fun build(
        userId: Long,
        rawLog: RawLog,
        matchedKeyword: String?,
    ): InsightContext {
        // 1) 최근 로그 윈도우 구성 (trigger 포함)
        val logLimit = properties.candidateWindowSize.coerceIn(5, 200)

        val recentLogs =
            logRepository.findLatestByUserId(
                userId = userId,
                pageable = PageRequest.of(0, logLimit),
            )

        // triggerLog가 recentLogs에 없을 수도 있으므로, 포함 보장
        // 암호화된 로그들을 복호화된 내용으로 변환
        val decryptedRawLog =
            RawLog(
                id = rawLog.id,
                userId = rawLog.userId,
                content = logEncryption.decrypt(rawLog.content),
                createdAt = rawLog.createdAt,
            )
        val decryptedRecentLogs =
            recentLogs.map { log ->
                RawLog(
                    id = log.id,
                    userId = log.userId,
                    content = logEncryption.decrypt(log.content),
                    createdAt = log.createdAt,
                )
            }

        val logs =
            (listOf(decryptedRawLog) + decryptedRecentLogs)
                .distinctBy { it.id } // RawLog에 id가 있다고 가정. 없다면 content+createdAt 등으로 대체
                .sortedByDescending { it.createdAt ?: Instant.EPOCH } // most recent first

        // 2) 최근 인사이트(중복 방지 입력) 구성
        val hours = properties.recentInsightsHours.coerceIn(6, 168) // 6h~7d
        val since = Instant.now(clock).minus(Duration.ofHours(hours.toLong()))

        val insights =
            insightRepository
                .findLatestByUserId(
                    userId = userId,
                    pageable = PageRequest.of(0, properties.maxRecentInsights.coerceIn(1, 10)),
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
            triggerLog = decryptedRawLog,
            sourceLogId = rawLog.id,
            logs = logs,
            recentInsights = insights,
        )
    }
}
