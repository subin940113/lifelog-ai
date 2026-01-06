package com.example.lifelog.insight.pipeline

import org.springframework.stereotype.Component

@Component
class InsightGeneratorRouter(
    private val props: InsightPolicyProperties,
    private val heuristic: HeuristicInsightGenerator,
    private val openai: OpenAiInsightGenerator,
) : InsightGenerator {
    override fun generate(ctx: InsightContext): GeneratedInsight? {
        val which = props.generator.trim().lowercase()
        return when (which) {
            "heuristic" -> heuristic.generate(ctx)
            "openai" -> openai.generate(ctx)
            "", "default" -> openai.generate(ctx)
            else -> openai.generate(ctx)
        }
    }
}
