package com.example.lifelog.infrastructure.external.signal

import com.example.lifelog.common.exception.ErrorCode
import com.example.lifelog.common.exception.NotFoundException
import org.springframework.stereotype.Component

@Component
class KeywordInsightPromptLoader {
    fun loadSystemPrompt(): String = read("/prompts/keyword_insight_system.txt")

    private fun loadUserTemplate(): String = read("/prompts/keyword_insight_user.txt")

    private fun read(resourcePath: String): String =
        javaClass.getResource(resourcePath)?.readText()
            ?: throw NotFoundException(ErrorCode.NOT_FOUND_RESOURCE, "Resource not found: $resourcePath")

    fun buildUserPrompt(
        keywordKey: String,
        recentLogs: List<String>,
        previousInsight: String?,
        topInsights: List<String>,
    ): String {
        val logsBlock = recentLogs.joinToString("\n") { "- $it" }.take(50_000)

        val topInsightsBlock =
            if (topInsights.isEmpty()) {
                "(none)"
            } else {
                topInsights.joinToString("\n") { "- $it" }.take(20_000)
            }

        return loadUserTemplate()
            .replace("{{keywordKey}}", keywordKey)
            .replace("{{previousInsight}}", previousInsight ?: "(none)")
            .replace("{{topInsights}}", topInsightsBlock)
            .replace("{{recentLogs}}", logsBlock)
            .trim()
    }
}
