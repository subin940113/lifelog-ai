package com.example.lifelog.insight.pipeline

import com.example.lifelog.insight.AiInsightKind
import org.springframework.stereotype.Component

@Component
class HeuristicInsightGenerator(
    private val props: InsightPolicyProperties,
) : InsightGenerator {
    override fun generate(ctx: InsightContext): GeneratedInsight? {
        val merged = ctx.logs.joinToString("\n\n") { it.content.trim() }.trim()
        if (merged.length < props.minChars) return null

        val matched = ctx.matchedKeyword?.trim().takeIf { !it.isNullOrBlank() } ?: return null

        val title = "‘$matched’가 자주 등장해요"
        val body = "최근 기록에서 ‘$matched’ 관련 표현이 반복됩니다. 지금 이 주제가 현재 상태를 설명하는 단서일 수 있어요."
        val evidence =
            merged
                .replace("\n", " ")
                .trim()
                .let { if (it.length <= 180) it else it.substring(0, 180).trimEnd() + "…" }

        return GeneratedInsight(
            kind = AiInsightKind.PATTERN,
            title = title,
            body = body,
            evidence = evidence,
            keyword = matched,
        )
    }
}
