package com.example.lifelog.domain.push

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

/**
 * 푸시 설정 도메인 엔티티
 */
@Entity
@Table(name = "push_setting")
class PushSetting(
    @Id
    @Column(name = "user_id")
    val userId: Long,
    @Column(name = "enabled", nullable = false)
    var enabled: Boolean = true,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
) {
    fun enable() {
        enabled = true
        updatedAt = Instant.now()
    }

    fun disable() {
        enabled = false
        updatedAt = Instant.now()
    }
}
