package com.example.lifelog.insight.settings

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

@Entity
@Table(
    name = "insight_settings",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_insight_settings_user_id", columnNames = ["user_id"]),
    ],
)
class InsightSettings(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "user_id", nullable = false)
    val userId: Long,
    @Column(name = "enabled", nullable = false)
    var enabled: Boolean = false,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
)
