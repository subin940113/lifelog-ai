package com.example.lifelog.infrastructure.external.insight

import com.example.lifelog.infrastructure.config.InsightPolicyProperties
import org.springframework.stereotype.Component

/**
 * 인사이트 프롬프트 로더
 */
@Component
class InsightPromptLoader(
    private val properties: InsightPolicyProperties,
) {
    fun loadSystemPrompt(): String = read(properties.prompt.systemPath)

    fun loadUserTemplate(): String = read(properties.prompt.userPath)

    fun loadSchemaJson(): String = read(properties.prompt.schemaPath)

    private fun read(resourcePath: String): String =
        javaClass.getResource(resourcePath)?.readText()
            ?: throw IllegalArgumentException("Resource not found: $resourcePath")
}
