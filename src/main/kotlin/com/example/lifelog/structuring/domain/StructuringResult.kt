package com.example.lifelog.structuring.domain

sealed class StructuringResult {
    data class Success(
        val drafts: List<StructuredEventDraft>,
        val meta: StructuringMeta = StructuringMeta(),
    ) : StructuringResult()

    data class Failure(
        val error: StructuringError,
        val meta: StructuringMeta = StructuringMeta(),
    ) : StructuringResult()
}

data class StructuringMeta(
    val structurer: String? = null, // "pseudo" | "openai"
    val promptVersion: String? = null,
    val model: String? = null,
    val latencyMs: Long? = null,
    val tokensIn: Int? = null,
    val tokensOut: Int? = null,
)
