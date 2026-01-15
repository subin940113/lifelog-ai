package com.example.lifelog.infrastructure.external.insight

import com.example.lifelog.application.insight.pipeline.InsightGenerator
import com.example.lifelog.domain.insight.GeneratedInsight
import com.example.lifelog.domain.insight.InsightContext
import com.example.lifelog.domain.insight.InsightKind
import com.example.lifelog.infrastructure.config.InsightPolicyProperties
import org.springframework.stereotype.Component

/**
 * 휴리스틱 기반 인사이트 생성기
 */
@Component
class HeuristicInsightGenerator(
    private val properties: InsightPolicyProperties,
) : InsightGenerator {
    override fun generate(ctx: InsightContext): GeneratedInsight? {
        val merged = ctx.logs.joinToString("\n\n") { it.content.trim() }.trim()
        if (merged.length < properties.minChars) return null

        val matched = ctx.matchedKeyword?.trim().takeIf { !it.isNullOrBlank() } ?: return null

        val title = "'$matched'가 자주 등장해요"
        val body = "최근 기록에서 '$matched' 관련 표현이 반복됩니다. 지금 이 주제가 현재 상태를 설명하는 단서일 수 있어요."
        val evidence =
            merged
                .replace("\n", " ")
                .trim()
                .let { if (it.length <= 180) it else it.substring(0, 180).trimEnd() + "…" }

        return GeneratedInsight(
            kind = InsightKind.PATTERN,
            title = title,
            body = body,
            evidence = evidence,
            keyword = matched,
        )
    }
}
