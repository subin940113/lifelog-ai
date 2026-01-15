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
 * 리프레시 토큰 도메인 엔티티
 */
@Entity
@Table(
    name = "refresh_tokens",
    indexes = [
        Index(name = "idx_refresh_tokens_user_id", columnList = "user_id"),
        Index(name = "idx_refresh_tokens_expires_at", columnList = "expires_at"),
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_refresh_tokens_token_hash", columnNames = ["token_hash"]),
    ],
)
class RefreshToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(name = "user_id", nullable = false)
    var userId: Long,
    @Column(name = "token_hash", nullable = false, length = 64)
    var tokenHash: String,
    @Column(name = "issued_at", nullable = false)
    var issuedAt: Instant,
    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant,
    @Column(name = "revoked_at")
    var revokedAt: Instant? = null,
    @Column(name = "replaced_by_hash", length = 64)
    var replacedByHash: String? = null,
) {
    fun isExpired(now: Instant = Instant.now()): Boolean = expiresAt.isBefore(now) || expiresAt == now

    fun isRevoked(): Boolean = revokedAt != null
}
