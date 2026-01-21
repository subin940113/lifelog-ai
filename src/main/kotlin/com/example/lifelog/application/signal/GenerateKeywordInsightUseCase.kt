package com.example.lifelog.application.signal

import com.example.lifelog.domain.insight.TopInsightQueryRepository
import com.example.lifelog.domain.log.LogRepository
import com.example.lifelog.domain.signal.KeywordSignalStateRepository
import com.example.lifelog.infrastructure.external.openai.OpenAiClient
import com.example.lifelog.infrastructure.external.signal.KeywordInsightPromptLoader
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GenerateKeywordInsightUseCase(
    private val stateRepository: KeywordSignalStateRepository,
    private val logRepository: LogRepository,
    private val topInsightQueryRepository: TopInsightQueryRepository,
    private val openAiClient: OpenAiClient,
    private val promptLoader: KeywordInsightPromptLoader,
    private val writer: KeywordInsightWriter,
) {
    private val thresholdDelta: Long = 10 // candy가 checkpoint 대비 이만큼 증가하면 재생성
    private val recentLogsLimit: Int = 200 // “최근 최대한 많이”의 현실적 상한
    private val topInsightsLimit: Int = 5 // 점수 높은 기존 인사이트 참조 상한

    @Transactional
    fun execute(userId: Long): Int {
        val candidates = stateRepository.findActiveByUserId(userId)

        var generatedCount = 0

        for (state in candidates) {
            // 엔티티 레벨에서 최종 방어
            if (!state.shouldRegenerateInsight(thresholdDelta)) continue

            val keywordKey = state.keywordKey

            // 최근 로그: 일단 "유저 최근 N개"를 가져오고, 키워드가 포함된 로그만 2차 필터
            // (RawLog.matched_keyword_keys 를 제거한 전제 하에서의 보수적 접근)
            val recentCandidates = logRepository.findLatestByUserId(userId, PageRequest.of(0, recentLogsLimit))
            val recentLogs =
                recentCandidates
                    .asSequence()
                    .filter { it.content.contains(keywordKey, ignoreCase = true) }
                    .map { it.content }
                    .toList()

            // Top liked insights: 반드시 (userId, keyword) 범위로 제한된 LIKE 인사이트만
            val topInsights =
                topInsightQueryRepository.findTopLikedInsightsByUserAndKeyword(
                    userId,
                    keywordKey,
                    PageRequest.of(0, topInsightsLimit),
                )

            val topInsightsText =
                topInsights.map { row ->
                    buildString {
                        append("title: ").append(row.title)
                        append("\n")
                        append("body: ").append(row.body)
                        if (!row.evidence.isNullOrBlank()) {
                            append("\n")
                            append("evidence: ").append(row.evidence)
                        }
                    }
                }

            val systemPrompt = promptLoader.loadSystemPrompt()
            val userPrompt =
                promptLoader.buildUserPrompt(
                    keywordKey = keywordKey,
                    recentLogs = recentLogs,
                    previousInsight = state.insightText,
                    topInsights = topInsightsText,
                )

            val insightText = openAiClient.structure(system = systemPrompt, user = userPrompt)

            writer.write(userId = userId, keywordKey = keywordKey, insightText = insightText)
            generatedCount += 1
        }

        return generatedCount
    }
}
