package com.example.lifelog.auth

import jakarta.persistence.*

@Entity
@Table(
    name = "oauth_accounts",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_oauth_provider_user",
            columnNames = ["provider", "provider_user_id"],
        )
    ]
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

    var email: String? = null,
    var displayName: String? = null,
    var pictureUrl: String? = null,
)