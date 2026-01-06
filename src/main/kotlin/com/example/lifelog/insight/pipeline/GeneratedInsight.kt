package com.example.lifelog.insight.pipeline

import com.example.lifelog.insight.AiInsightKind

data class GeneratedInsight(
    val kind: AiInsightKind,
    val title: String,
    val body: String,
    val evidence: String? = null,
    val keyword: String? = null,
)
