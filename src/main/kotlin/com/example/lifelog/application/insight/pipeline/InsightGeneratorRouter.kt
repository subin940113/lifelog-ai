package com.example.lifelog.application.insight.pipeline

import com.example.lifelog.domain.insight.GeneratedInsight
import com.example.lifelog.domain.insight.InsightContext
import com.example.lifelog.infrastructure.config.InsightPolicyProperties
import com.example.lifelog.infrastructure.external.insight.HeuristicInsightGenerator
import com.example.lifelog.infrastructure.external.insight.OpenAiInsightGenerator
import org.springframework.stereotype.Component

@Component
class InsightGeneratorRouter(
    private val properties: InsightPolicyProperties,
    private val heuristic: HeuristicInsightGenerator,
    private val openai: OpenAiInsightGenerator,
) : InsightGenerator {
    override fun generate(ctx: InsightContext): GeneratedInsight? {
        val generatorType = properties.generator.trim().lowercase()
        return when (generatorType) {
            "heuristic" -> heuristic.generate(ctx)
            "openai" -> openai.generate(ctx)
            "", "default" -> openai.generate(ctx)
            else -> openai.generate(ctx)
        }
    }
}
