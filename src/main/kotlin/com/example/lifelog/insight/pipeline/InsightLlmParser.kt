package com.example.lifelog.insight.pipeline

import com.example.lifelog.insight.AiInsightKind
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

data class LlmInsightJson(
    val kind: String?,
    val title: String?,
    val body: String?,
    val evidence: String?,
    val keyword: String?,
)

@Component
class InsightLlmParser(
    private val objectMapper: ObjectMapper,
) {
    fun parse(json: String): GeneratedInsight? {
        val node = runCatching { objectMapper.readValue(json, LlmInsightJson::class.java) }.getOrNull() ?: return null

        val title = node.title?.trim().orEmpty()
        val body = node.body?.trim().orEmpty()
        if (title.isBlank() || body.isBlank()) return null

        val kind =
            node.kind
                ?.trim()
                ?.uppercase()
                ?.let { runCatching { AiInsightKind.valueOf(it) }.getOrNull() }
                ?: AiInsightKind.PATTERN

        return GeneratedInsight(
            kind = kind,
            title = title.take(120),
            body = body.take(2000), // ✅ 길이 상향
            evidence = node.evidence?.trim()?.take(500), // (선택) 상향
            keyword = node.keyword?.trim()?.take(60),
        )
    }
}
