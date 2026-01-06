package com.example.lifelog.insight.pipeline

import org.springframework.boot.context.properties.ConfigurationProperties

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

    /**
     * master switch for LLM usage (openai generator will short-circuit if false)
     */
    var llmEnabled: Boolean = true

    var prompt: PromptProps = PromptProps()

    class PromptProps {
        var systemPath: String = "/prompts/insight_system.txt"
        var userPath: String = "/prompts/insight_user.txt"
        var schemaPath: String = "/schemas/insight-schema.json"
    }
}
