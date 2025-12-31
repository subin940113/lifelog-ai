package com.example.lifelog.auth

import org.springframework.data.jpa.repository.JpaRepository

interface OAuthAccountRepository : JpaRepository<OAuthAccount, Long> {
    fun findByProviderAndProviderUserId(
        provider: OAuthProvider,
        providerUserId: String,
    ): OAuthAccount?
}
