package com.example.lifelog.log.structured

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface StructuredEventRepository : JpaRepository<StructuredEvent, Long> {
    fun findAllByUserIdOrderByOccurredAtDesc(userId: Long): List<StructuredEvent>

    fun findAllByUserIdAndCategoryOrderByOccurredAtDesc(
        userId: Long,
        category: String,
    ): List<StructuredEvent>

    fun findAllByUserIdAndRawLogIdOrderByCreatedAtDesc(
        userId: Long,
        rawLogId: Long,
    ): List<StructuredEvent>

    @Query(
        """
    select e.category as category, count(e) as count
    from StructuredEvent e
    where e.occurredAt between :start and :end
    group by e.category
    """,
    )
    fun countByCategoryBetween(
        @Param("start") start: Instant,
        @Param("end") end: Instant,
    ): List<CategoryCountProjection>
}
