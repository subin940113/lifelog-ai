package com.example.lifelog.insight.pipeline

import com.example.lifelog.insight.AiInsightKind
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

data class LlmInsightJson(
    val should_generate: Boolean? = null,
    val skip_reason: String? = null,
    val kind: String? = null,
    val title: String? = null,
    val body: String? = null,
    val evidence: String? = null,
    val keyword: String? = null,
)

@Component
class InsightLlmParser(
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(InsightLlmParser::class.java)

    fun parse(json: String): GeneratedInsight? {
        val node =
            runCatching { objectMapper.readValue(json, LlmInsightJson::class.java) }
                .getOrNull()
                ?: run {
                    log.debug("Insight parse failed: invalid_json")
                    return null
                }

        // 1) 모델이 스킵을 선택한 경우: null 반환
        if (node.should_generate == false) {
            log.debug("Insight skipped by model. reason={}", node.skip_reason ?: "UNKNOWN")
            return null
        }

        // (선택) should_generate 누락을 점진적으로 금지하고 싶다면:
        // if (node.should_generate == null && !props.allowLegacyWithoutShouldGenerate) return null

        // 2) should_generate=true/누락이어도 title/body 없으면 drop
        val title = node.title?.trim().orEmpty()
        val body = node.body?.trim().orEmpty()
        if (title.isBlank() || body.isBlank()) {
            log.debug(
                "Insight dropped: missing_title_or_body (should_generate={}, skip_reason={})",
                node.should_generate,
                node.skip_reason,
            )
            return null
        }

        // 3) kind 파싱: 실패 시 PATTERN
        val kind =
            node.kind
                ?.trim()
                ?.uppercase()
                ?.let { runCatching { AiInsightKind.valueOf(it) }.getOrNull() }
                ?: AiInsightKind.PATTERN

        // 4) keyword/evidence는 빈 문자열 저장 방지
        val evidence =
            node.evidence
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?.take(500)
        val keyword =
            node.keyword
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?.take(60)

        return GeneratedInsight(
            kind = kind,
            title = title.take(120),
            body = body.take(2000),
            evidence = evidence,
            keyword = keyword,
        )
    }
}
