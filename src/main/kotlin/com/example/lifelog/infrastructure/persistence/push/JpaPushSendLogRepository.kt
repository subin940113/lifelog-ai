package com.example.lifelog.infrastructure.persistence.push

import com.example.lifelog.domain.push.PushSendLog
import com.example.lifelog.domain.push.PushSendType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface JpaPushSendLogRepository : JpaRepository<PushSendLog, Long> {
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

    @Query(
        """
        select exists(
            select 1
            from PushSendLog l
            where l.userId = :userId
              and l.type = :type
              and l.keyword = :keyword
              and l.localDate >= :localDate
        )
        """,
    )
    fun existsByUserIdAndTypeAndKeywordAndLocalDateGreaterThanEqual(
        @Param("userId") userId: Long,
        @Param("type") type: PushSendType,
        @Param("keyword") keyword: String,
        @Param("localDate") localDate: LocalDate,
    ): Boolean
}
