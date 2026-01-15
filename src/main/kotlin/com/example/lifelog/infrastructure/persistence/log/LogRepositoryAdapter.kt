package com.example.lifelog.infrastructure.persistence.log

import com.example.lifelog.domain.log.LogRepository
import com.example.lifelog.domain.log.LogSlice
import com.example.lifelog.domain.log.RawLog
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * LogRepository의 JPA 구현을 위한 어댑터
 * 인터페이스에 default 메서드가 필요한 경우 사용
 */
@Component
class LogRepositoryAdapter(
    private val jpaRepository: JpaLogRepository,
) : LogRepository {
    override fun save(log: RawLog): RawLog = jpaRepository.save(log)

    override fun findById(id: Long): RawLog? = jpaRepository.findById(id).orElse(null)

    override fun findFirstPage(userId: Long, pageable: Pageable): List<RawLog> =
        jpaRepository.findFirstPage(userId, pageable)

    override fun findNextPage(
        userId: Long,
        cursorCreatedAt: Instant,
        cursorId: Long,
        pageable: Pageable,
    ): List<RawLog> = jpaRepository.findNextPage(userId, cursorCreatedAt, cursorId, pageable)

    override fun countByUserIdAndCreatedAtGreaterThanEqual(
        userId: Long,
        createdAt: Instant,
    ): Long = jpaRepository.countByUserIdAndCreatedAtGreaterThanEqual(userId, createdAt)

    override fun findByUserIdOrderByCreatedAtDesc(
        userId: Long,
        pageable: Pageable,
    ): List<RawLog> = jpaRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)

    override fun findSliceBetween(
        userId: Long,
        start: Instant,
        end: Instant,
        pageable: Pageable,
    ): List<LogSlice> {
        return jpaRepository.findSliceBetweenInternal(userId, start, end, pageable)
            .map { LogSlice(it.createdAt, it.content) }
    }

    override fun existsByUserIdAndCreatedAtBetween(
        userId: Long,
        start: Instant,
        end: Instant,
    ): Boolean = jpaRepository.existsByUserIdAndCreatedAtBetween(userId, start, end)

    override fun findLatestByUserId(
        userId: Long,
        pageable: Pageable,
    ): List<RawLog> = jpaRepository.findLatestByUserId(userId, pageable)
}
