package com.example.lifelog.insight

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface AggregatedInsightRepository : JpaRepository<AggregatedInsight, Long> {
    fun existsByWeekStartDate(weekStartDate: LocalDate): Boolean
}
