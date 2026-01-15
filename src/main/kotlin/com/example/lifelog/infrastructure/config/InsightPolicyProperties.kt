package com.example.lifelog.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 인사이트 정책 설정 Properties
 */
@ConfigurationProperties(prefix = "lifelog.insight")
class InsightPolicyProperties {
    /**
     * which generator to use:
     * - "heuristic"
     * - "openai"
     */
    var generator: String = "openai"

    var candidateWindowSize: Int = 80
    var maxSelectedLogs: Int = 18
    var minChars: Int = 80

    var cooldownSeconds: Long = 3600
    var dailyLimit: Int = 5
    var keywordMatchRequired: Boolean = true

    val llmEnabled: Boolean = true

    // RECENT_INSIGHTS
    val recentInsightsHours: Int = 48
    val maxRecentInsights: Int = 5

    // 언어 fallback
    val defaultLangCode: String = "ko"

    val dislikeStreakConservativeThreshold: Int = 3

    var prompt: PromptProps = PromptProps()

    class PromptProps {
        var systemPath: String = "/prompts/insight_system.txt"
        var userPath: String = "/prompts/insight_user.txt"
        var schemaPath: String = "/schemas/insight-schema.json"
    }
}
