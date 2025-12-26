package com.example.lifelog.log.structured

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant

@Entity
@Table(name = "structured_event")
class StructuredEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "user_id", nullable = false)
    val userId: Long,
    @Column(name = "raw_log_id", nullable = false)
    val rawLogId: Long,
    @Column(nullable = false)
    val category: String,
    @Column(name = "occurred_at")
    val occurredAt: Instant?,
    @Column(nullable = false)
    val confidence: Double,
    @field:JdbcTypeCode(SqlTypes.JSON)
    @field:Column(columnDefinition = "jsonb")
    val payload: String,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),
)
