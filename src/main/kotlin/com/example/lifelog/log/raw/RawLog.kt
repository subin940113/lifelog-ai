package com.example.lifelog.log.raw

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "raw_log")
class RawLog(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(nullable = false, columnDefinition = "text")
    val content: String,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)