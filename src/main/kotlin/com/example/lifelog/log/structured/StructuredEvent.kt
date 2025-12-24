package com.example.lifelog.log.structured

import jakarta.persistence.*
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

    @Column(nullable = false, columnDefinition = "jsonb")
    val payload: String,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)