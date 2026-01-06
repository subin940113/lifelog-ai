package com.example.lifelog.log.raw

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface RawLogRepository : JpaRepository<RawLog, Long> {
    // ✅ 모드2(WINDOW)에서 최근 N개를 가져오기 위한 메서드
    // createdAt desc, id desc 로 정렬해서 안정적으로 최신순
    @Query(
        """
        select r
        from RawLog r
        where r.userId = :userId
        order by r.createdAt desc, r.id desc
        """,
    )
    fun findRecentWindow(
        @Param("userId") userId: Long,
        pageable: Pageable,
    ): List<RawLog>

    // ✅ 기존 list API(cursor)에서 사용하던 메서드들 (유지)
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
}
