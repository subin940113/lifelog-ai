package com.example.lifelog.log.raw

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface RawLogRepository : JpaRepository<RawLog, Long> {
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
        @Param("cursorCreatedAt") cursorCreatedAt: java.time.Instant,
        @Param("cursorId") cursorId: Long,
        pageable: Pageable,
    ): List<RawLog>

    fun countByUserIdAndCreatedAtGreaterThanEqual(
        userId: Long,
        createdAt: java.time.Instant,
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
    fun findSliceBetween(
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
