package com.example.lifelog.domain.auth

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

/**
 * 애플 refresh_token 도메인 엔티티
 * 애플 계정 삭제 시 revoke에 사용
 */
@Entity
@Table(
    name = "apple_refresh_tokens",
    indexes = [
        Index(name = "idx_apple_refresh_tokens_user_id", columnList = "user_id"),
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_apple_refresh_tokens_user_id", columnNames = ["user_id"]),
    ],
)
class AppleRefreshToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(name = "user_id", nullable = false, unique = true)
    var userId: Long,
    @Column(name = "refresh_token", nullable = false, columnDefinition = "text")
    var refreshToken: String,
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
)
