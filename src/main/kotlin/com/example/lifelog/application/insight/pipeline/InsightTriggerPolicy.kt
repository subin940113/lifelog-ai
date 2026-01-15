package com.example.lifelog.application.insight.pipeline

import com.example.lifelog.domain.insight.InsightTriggerDecision
import com.example.lifelog.domain.log.RawLog

/**
 * 인사이트 트리거 정책 인터페이스
 */
interface InsightTriggerPolicy {
    fun decide(
        userId: Long,
        rawLog: RawLog,
    ): InsightTriggerDecision
}
