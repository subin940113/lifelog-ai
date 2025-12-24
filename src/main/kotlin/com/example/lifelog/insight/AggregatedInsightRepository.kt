package com.example.lifelog.insight

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.time.LocalDateTime

interface AggregatedInsightRepository : JpaRepository<AggregatedInsight, Long> {
    fun existsByWeekStartDate(weekStartDate: LocalDate): Boolean

    @Query(
        """
    select e.category as category, count(e) as count
    from StructuredEvent e
    where e.occurredAt between :start and :end
    group by e.category
    """
    )
    fun countByCategoryBetween(
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<CategoryCountProjection>
}