package com.example.lifelog.domain.auth

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

/**
 * OAuth 계정 도메인 엔티티
 */
@Entity
@Table(
    name = "oauth_accounts",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_oauth_provider_user",
            columnNames = ["provider", "provider_user_id"],
        ),
    ],
)
class OAuthAccount(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var provider: OAuthProvider,
    @Column(name = "provider_user_id", nullable = false)
    var providerUserId: String,
    @Column(name = "user_id", nullable = false)
    var userId: Long,
)
