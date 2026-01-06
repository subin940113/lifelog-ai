package com.example.lifelog.insight.pipeline

import com.example.lifelog.log.raw.RawLog

data class InsightContext(
    val userId: Long,
    val matchedKeyword: String?,
    val triggerLog: RawLog,
    val sourceLogId: Long,
    val logs: List<RawLog>,
)
