package com.example.lifelog.insight.pipeline

import com.example.lifelog.log.raw.RawLog

interface InsightContextBuilder {
    fun build(
        userId: Long,
        rawLog: RawLog,
        matchedKeyword: String?,
    ): InsightContext
}
