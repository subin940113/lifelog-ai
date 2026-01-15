package com.example.lifelog.push

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

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
)
