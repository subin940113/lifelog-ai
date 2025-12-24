package com.example.lifelog.log.event

import com.example.lifelog.log.raw.RawLog

/**
 * RawLog 저장 성공 후 발행되는 도메인 이벤트
 */
data class RawLogCreatedEvent(
    val rawLog: RawLog
)