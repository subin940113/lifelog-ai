package com.example.lifelog.infrastructure.persistence.auth

import com.example.lifelog.domain.auth.OAuthAccount
import com.example.lifelog.domain.auth.OAuthAccountRepository
import com.example.lifelog.domain.auth.OAuthProvider
import org.springframework.stereotype.Component

/**
 * OAuthAccountRepository JPA 어댑터
 */
@Component
class OAuthAccountRepositoryAdapter(
    private val jpaRepo: JpaOAuthAccountRepository,
) : OAuthAccountRepository {
    override fun findByProviderAndProviderUserId(
        provider: OAuthProvider,
        providerUserId: String,
    ): OAuthAccount? = jpaRepo.findByProviderAndProviderUserId(provider, providerUserId)

    override fun save(account: OAuthAccount): OAuthAccount = jpaRepo.save(account)

    override fun deleteAllByUserId(userId: Long) = jpaRepo.deleteAllByUserId(userId)
}
