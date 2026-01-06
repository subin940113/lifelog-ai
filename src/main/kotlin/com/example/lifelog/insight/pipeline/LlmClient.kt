package com.example.lifelog.insight.pipeline

interface LlmClient {
    fun generateJson(
        model: String,
        prompt: String,
        timeoutMs: Long,
        maxTokens: Int,
    ): String?
}
