package com.example.lifelog.insight

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(
    name = "aggregated_insight",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["week_start_date"]),
    ],
)
class AggregatedInsight(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "week_start_date", nullable = false)
    val weekStartDate: LocalDate,
    @Column(name = "week_end_date", nullable = false)
    val weekEndDate: LocalDate,
    @Column(nullable = false)
    val totalEventCount: Long,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    val categoryCounts: Map<String, Long> = emptyMap(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
