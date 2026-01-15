package com.example.lifelog.application.insight.pipeline

import com.example.lifelog.domain.insight.InsightContext
import com.example.lifelog.domain.log.RawLog

/**
 * 인사이트 컨텍스트 빌더 인터페이스
 */
interface InsightContextBuilder {
    fun build(
        userId: Long,
        rawLog: RawLog,
        matchedKeyword: String?,
    ): InsightContext
}
