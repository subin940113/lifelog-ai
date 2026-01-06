package com.example.lifelog.insight.pipeline

import com.example.lifelog.log.raw.RawLog

interface InsightTriggerPolicy {
    fun decide(
        userId: Long,
        rawLog: RawLog,
    ): InsightTriggerDecision
}
