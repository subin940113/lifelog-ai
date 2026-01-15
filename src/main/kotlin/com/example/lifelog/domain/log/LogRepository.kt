package com.example.lifelog.domain.log

import org.springframework.data.domain.Pageable
import java.time.Instant

/**
 * Log 도메인 리포지토리 인터페이스
 * 구현체는 infrastructure 레이어에 위치
 */
interface LogRepository {
    fun save(log: RawLog): RawLog

    fun findById(id: Long): RawLog?

    fun findFirstPage(
        userId: Long,
        pageable: Pageable,
    ): List<RawLog>

    fun findNextPage(
        userId: Long,
        cursorCreatedAt: Instant,
        cursorId: Long,
        pageable: Pageable,
    ): List<RawLog>

    fun countByUserIdAndCreatedAtGreaterThanEqual(
        userId: Long,
        createdAt: Instant,
    ): Long

    fun findByUserIdOrderByCreatedAtDesc(
        userId: Long,
        pageable: Pageable,
    ): List<RawLog>

    fun findSliceBetween(
        userId: Long,
        start: Instant,
        end: Instant,
        pageable: Pageable,
    ): List<LogSlice>

    fun existsByUserIdAndCreatedAtBetween(
        userId: Long,
        start: Instant,
        end: Instant,
    ): Boolean

    fun findLatestByUserId(
        userId: Long,
        pageable: Pageable,
    ): List<RawLog>
}

/**
 * 로그 슬라이스 (createdAt, content만 필요한 경우)
 */
data class LogSlice(
    val createdAt: Instant,
    val content: String,
)
