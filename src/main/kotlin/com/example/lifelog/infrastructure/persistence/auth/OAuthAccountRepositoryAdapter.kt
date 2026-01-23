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
    private val jpaRepository: JpaOAuthAccountRepository,
) : OAuthAccountRepository {
    override fun findByProviderAndProviderUserId(
        provider: OAuthProvider,
        providerUserId: String,
    ): OAuthAccount? = jpaRepository.findByProviderAndProviderUserId(provider, providerUserId)

    override fun findByUserId(userId: Long): List<OAuthAccount> = jpaRepository.findByUserId(userId)

    override fun save(account: OAuthAccount): OAuthAccount = jpaRepository.save(account)

    override fun deleteAllByUserId(userId: Long) = jpaRepository.deleteAllByUserId(userId)
}
