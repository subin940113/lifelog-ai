package com.example.lifelog.infrastructure.persistence.insight

import com.example.lifelog.domain.insight.Insight
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant

/**
 * Insight 도메인 리포지토리 JPA 구현체
 */
@Repository
interface JpaInsightRepository : JpaRepository<Insight, Long> {
    @Query(
        """
        select a
          from Insight a
         where a.userId = :userId
         order by a.createdAt desc
        """,
    )
    fun findLatestByUserId(
        @Param("userId") userId: Long,
        pageable: Pageable,
    ): List<Insight>

    @Query(
        """
        select a
        from Insight a
        where a.userId = :userId
        order by a.createdAt desc, a.id desc
        """,
    )
    fun findFirstPage(
        @Param("userId") userId: Long,
        pageable: Pageable,
    ): List<Insight>

    @Query(
        """
        select a
        from Insight a
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
    ): List<Insight>
}
