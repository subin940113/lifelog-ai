package com.example.lifelog.domain.log

/**
 * RawLog 저장 성공 후 발행되는 도메인 이벤트
 */
data class RawLogCreatedEvent(
    val rawLog: RawLog,
)
