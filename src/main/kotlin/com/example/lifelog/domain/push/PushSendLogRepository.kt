package com.example.lifelog.domain.push

import java.time.LocalDate

/**
 * 푸시 발송 로그 도메인 리포지토리 인터페이스
 * 구현체는 infrastructure 레이어에 위치
 */
interface PushSendLogRepository {
    fun existsByUserIdAndTypeAndLocalDate(
        userId: Long,
        type: PushSendType,
        localDate: LocalDate,
    ): Boolean

    fun countByUserIdAndTypeAndLocalDate(
        userId: Long,
        type: PushSendType,
        localDate: LocalDate,
    ): Long

    fun existsByUserIdAndTypeAndLocalDateAndKeyword(
        userId: Long,
        type: PushSendType,
        localDate: LocalDate,
        keyword: String,
    ): Boolean

    fun existsByUserIdAndTypeAndKeywordAndLocalDateGreaterThanEqual(
        userId: Long,
        type: PushSendType,
        keyword: String,
        localDate: LocalDate,
    ): Boolean

    fun save(log: PushSendLog): PushSendLog
}
