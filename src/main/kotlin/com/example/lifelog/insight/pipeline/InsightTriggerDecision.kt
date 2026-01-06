package com.example.lifelog.insight.pipeline

data class InsightTriggerDecision(
    val shouldRun: Boolean,
    val reason: String? = null,
    val matchedKeyword: String? = null,
)
