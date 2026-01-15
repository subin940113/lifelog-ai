package com.example.lifelog.infrastructure.persistence.auth

import com.example.lifelog.domain.auth.RefreshToken
import com.example.lifelog.domain.auth.RefreshTokenRepository
import org.springframework.stereotype.Component

/**
 * RefreshTokenRepository JPA 어댑터
 */
@Component
class RefreshTokenRepositoryAdapter(
    private val jpaRepo: JpaRefreshTokenRepository,
) : RefreshTokenRepository {
    override fun findByTokenHash(tokenHash: String): RefreshToken? = jpaRepo.findByTokenHash(tokenHash).orElse(null)

    override fun save(token: RefreshToken): RefreshToken = jpaRepo.save(token)

    override fun deleteAllByUserId(userId: Long) = jpaRepo.deleteAllByUserId(userId)
}
