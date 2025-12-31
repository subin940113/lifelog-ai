package com.example.lifelog.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(name = "display_name", nullable = false)
    var displayName: String,
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),
    @Column(name = "last_login_at", nullable = false, updatable = true)
    var lastLoginAt: Instant = Instant.now(),
) {
    fun updateLastLoginAt() {
        lastLoginAt = Instant.now()
    }
}
