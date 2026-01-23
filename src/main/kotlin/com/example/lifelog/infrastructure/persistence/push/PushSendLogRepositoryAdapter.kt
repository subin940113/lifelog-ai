package com.example.lifelog.infrastructure.persistence.push

import com.example.lifelog.domain.push.PushSendLog
import com.example.lifelog.domain.push.PushSendLogRepository
import com.example.lifelog.domain.push.PushSendType
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * PushSendLogRepository JPA 어댑터
 */
@Component
class PushSendLogRepositoryAdapter(
    private val jpaRepository: JpaPushSendLogRepository,
) : PushSendLogRepository {
    override fun existsByUserIdAndTypeAndLocalDate(
        userId: Long,
        type: PushSendType,
        localDate: LocalDate,
    ): Boolean = jpaRepository.existsByUserIdAndTypeAndLocalDate(userId, type, localDate)

    override fun countByUserIdAndTypeAndLocalDate(
        userId: Long,
        type: PushSendType,
        localDate: LocalDate,
    ): Long = jpaRepository.countByUserIdAndTypeAndLocalDate(userId, type, localDate)

    override fun existsByUserIdAndTypeAndLocalDateAndKeyword(
        userId: Long,
        type: PushSendType,
        localDate: LocalDate,
        keyword: String,
    ): Boolean = jpaRepository.existsByUserIdAndTypeAndLocalDateAndKeyword(userId, type, localDate, keyword)

    override fun existsByUserIdAndTypeAndKeywordAndLocalDateGreaterThanEqual(
        userId: Long,
        type: PushSendType,
        keyword: String,
        localDate: LocalDate,
    ): Boolean = jpaRepository.existsByUserIdAndTypeAndKeywordAndLocalDateGreaterThanEqual(userId, type, keyword, localDate)

    override fun save(log: PushSendLog): PushSendLog = jpaRepository.save(log)
}
