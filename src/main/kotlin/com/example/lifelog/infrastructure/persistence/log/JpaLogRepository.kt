package com.example.lifelog.infrastructure.persistence.log

import com.example.lifelog.domain.log.RawLog
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant

/**
 * Log 도메인 리포지토리 JPA 구현체
 */
@Repository
interface JpaLogRepository : JpaRepository<RawLog, Long> {
    @Query(
        """
        select r
        from RawLog r
        where r.userId = :userId
        order by r.createdAt desc, r.id desc
        """,
    )
    fun findFirstPage(
        @Param("userId") userId: Long,
        pageable: Pageable,
    ): List<RawLog>

    @Query(
        """
        select r
        from RawLog r
        where r.userId = :userId
          and (r.createdAt < :cursorCreatedAt
               or (r.createdAt = :cursorCreatedAt and r.id < :cursorId))
        order by r.createdAt desc, r.id desc
        """,
    )
    fun findNextPage(
        @Param("userId") userId: Long,
        @Param("cursorCreatedAt") cursorCreatedAt: Instant,
        @Param("cursorId") cursorId: Long,
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

    interface RawLogSlice {
        val createdAt: Instant
        val content: String
    }

    @Query(
        """
        select r.createdAt as createdAt, r.content as content
        from RawLog r
        where r.userId = :userId
          and r.createdAt >= :start
          and r.createdAt < :end
        order by r.createdAt desc
        """,
    )
    fun findSliceBetweenInternal(
        @Param("userId") userId: Long,
        @Param("start") start: Instant,
        @Param("end") end: Instant,
        pageable: Pageable,
    ): List<RawLogSlice>

    fun existsByUserIdAndCreatedAtBetween(
        userId: Long,
        start: Instant,
        end: Instant,
    ): Boolean

    @Query(
        """
            select r
            from RawLog r
            where r.userId = :userId
            order by r.createdAt desc, r.id desc
        """,
    )
    fun findLatestByUserId(
        @Param("userId") userId: Long,
        pageable: Pageable,
    ): List<RawLog>
}
