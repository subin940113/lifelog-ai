package com.example.lifelog.insight.pipeline

import org.springframework.stereotype.Component

@Component
class InsightPromptLoader(
    private val props: InsightPolicyProperties,
) {
    fun loadSystemPrompt(): String = read(props.prompt.systemPath)

    fun loadUserTemplate(): String = read(props.prompt.userPath)

    fun loadSchemaJson(): String = read(props.prompt.schemaPath)

    private fun read(resourcePath: String): String =
        javaClass.getResource(resourcePath)?.readText()
            ?: throw IllegalArgumentException("Resource not found: $resourcePath")
}
