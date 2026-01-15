package com.example.lifelog.domain.push

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant
import java.time.LocalDate

/**
 * 푸시 발송 로그 도메인 엔티티
 */
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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "user_id", nullable = false)
    val userId: Long,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: PushSendType,
    @Column(name = "local_date", nullable = false)
    val localDate: LocalDate,
    @Column(nullable = true, length = 80)
    val keyword: String? = null,
    @Column(nullable = false)
    val sentAt: Instant = Instant.now(),
)

/**
 * 푸시 발송 타입
 */
enum class PushSendType {
    TIME_PATTERN_MISS,
    KEYWORD_NUDGE,
    INSIGHT_CREATED,
}
