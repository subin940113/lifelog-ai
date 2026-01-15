package com.example.lifelog.push

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(
    name = "push_send_log",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "type", "local_date", "keyword"]),
    ],
    indexes = [
        Index(name = "idx_push_send_log_user_type_date", columnList = "user_id, type, local_date"),
    ],
)
class PushSendLog(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "user_id", nullable = false)
    val userId: Long,
    @Column(nullable = false)
    val type: String, // HABIT_MISS, KEYWORD_NUDGE
    @Column(name = "local_date", nullable = false)
    val localDate: LocalDate,
    @Column(nullable = true, length = 80)
    val keyword: String? = null,
    @Column(nullable = false)
    val sentAt: Instant = Instant.now(),
)
