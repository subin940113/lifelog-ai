package com.example.lifelog.push

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface PushSendLogRepository : JpaRepository<PushSendLog, Long> {
    fun existsByUserIdAndTypeAndLocalDate(
        userId: Long,
        type: String,
        localDate: LocalDate,
    ): Boolean

    fun countByUserIdAndTypeAndLocalDate(
        userId: Long,
        type: String,
        localDate: LocalDate,
    ): Long

    fun existsByUserIdAndTypeAndLocalDateAndKeyword(
        userId: Long,
        type: String,
        localDate: LocalDate,
        keyword: String,
    ): Boolean

    // 키워드 쿨다운(최근 N일 동안 같은 keyword로 보냈는지) 체크용
    fun existsByUserIdAndTypeAndKeywordAndLocalDateGreaterThanEqual(
        userId: Long,
        type: String,
        keyword: String,
        localDate: LocalDate,
    ): Boolean
}
