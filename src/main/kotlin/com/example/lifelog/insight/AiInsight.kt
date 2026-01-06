package com.example.lifelog.insight

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(
    name = "ai_insight",
    indexes = [
        Index(name = "idx_ai_insight_user_created_at", columnList = "user_id, created_at desc"),
        Index(name = "idx_ai_insight_user_keyword", columnList = "user_id, keyword"),
    ],
)
class AiInsight(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "user_id", nullable = false)
    val userId: Long,
    @Column(name = "source_log_id")
    val sourceLogId: Long? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val kind: AiInsightKind,
    @Column(nullable = false, length = 120)
    val title: String,
    @Column(nullable = false, columnDefinition = "text")
    val body: String,
    @Column(columnDefinition = "text")
    val evidence: String? = null,
    @Column(length = 60)
    val keyword: String? = null,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),
)
