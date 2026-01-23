package com.example.lifelog.infrastructure.persistence.auth

import com.example.lifelog.domain.auth.OAuthAccount
import com.example.lifelog.domain.auth.OAuthProvider
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaOAuthAccountRepository : JpaRepository<OAuthAccount, Long> {
    fun findByProviderAndProviderUserId(
        provider: OAuthProvider,
        providerUserId: String,
    ): OAuthAccount?

    fun findByUserId(userId: Long): List<OAuthAccount>

    fun deleteAllByUserId(userId: Long)
}
