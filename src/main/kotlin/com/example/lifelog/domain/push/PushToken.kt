package com.example.lifelog.domain.push

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

/**
 * 푸시 토큰 도메인 엔티티
 */
@Entity
@Table(
    name = "push_token",
    indexes = [
        Index(name = "idx_push_token_user", columnList = "user_id"),
        Index(name = "idx_push_token_token", columnList = "token"),
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_push_token_user_token", columnNames = ["user_id", "token"]),
    ],
)
class PushToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "user_id", nullable = false)
    var userId: Long,
    @Column(name = "token", nullable = false, length = 512)
    var token: String,
    @Column(name = "platform", nullable = true, length = 32)
    var platform: String? = null, // android | ios | web (optional)
    @Column(name = "enabled", nullable = false)
    var enabled: Boolean = true,
    @Column(name = "last_seen_at", nullable = false)
    var lastSeenAt: Instant = Instant.now(),
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
) {
    @PrePersist
    fun prePersist() {
        val now = Instant.now()
        createdAt = now
        updatedAt = now
        lastSeenAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = Instant.now()
    }

    fun disable() {
        enabled = false
    }

    fun enable() {
        enabled = true
    }

    fun updateLastSeen() {
        lastSeenAt = Instant.now()
    }
}
