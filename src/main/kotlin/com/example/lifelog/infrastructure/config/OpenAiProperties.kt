package com.example.lifelog.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * OpenAI 설정 Properties
 */
@ConfigurationProperties(prefix = "openai")
data class OpenAiProperties(
    val apiKey: String,
    val baseUrl: String,
    val model: String,
    val timeoutSeconds: Long = 30,
)
