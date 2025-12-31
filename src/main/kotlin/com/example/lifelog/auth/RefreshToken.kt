package com.example.lifelog.auth

import jakarta.persistence.*
import java.time.Instant

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
