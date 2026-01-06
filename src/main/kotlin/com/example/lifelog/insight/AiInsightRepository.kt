package com.example.lifelog.insight

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface AiInsightRepository : JpaRepository<AiInsight, Long> {
    fun findByUserIdOrderByCreatedAtDesc(
        userId: Long,
        pageable: Pageable,
    ): List<AiInsight>

    /**
     * MVP(Heuristic) 단계: 상태/기간 없이, 유저의 최신 인사이트를 createdAt desc로 가져온다.
     */
    @Query(
        """
        select a
          from AiInsight a
         where a.userId = :userId
         order by a.createdAt desc
        """,
    )
    fun findLatestByUserId(
        @Param("userId") userId: Long,
        pageable: Pageable,
    ): List<AiInsight>

    @Query(
        """
        select a
        from AiInsight a
        where a.userId = :userId
        order by a.createdAt desc, a.id desc
        """,
    )
    fun findFirstPage(
        @Param("userId") userId: Long,
        pageable: Pageable,
    ): List<AiInsight>

    @Query(
        """
        select a
        from AiInsight a
        where a.userId = :userId
          and (
            a.createdAt < :cursorCreatedAt
            or (a.createdAt = :cursorCreatedAt and a.id < :cursorId)
          )
        order by a.createdAt desc, a.id desc
        """,
    )
    fun findNextPage(
        @Param("userId") userId: Long,
        @Param("cursorCreatedAt") cursorCreatedAt: Instant,
        @Param("cursorId") cursorId: Long,
        pageable: Pageable,
    ): List<AiInsight>
}
