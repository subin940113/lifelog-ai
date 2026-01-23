package com.example.lifelog.infrastructure.persistence.auth

import com.example.lifelog.domain.auth.RefreshToken
import com.example.lifelog.domain.auth.RefreshTokenRepository
import org.springframework.stereotype.Component

/**
 * RefreshTokenRepository JPA 어댑터
 */
@Component
class RefreshTokenRepositoryAdapter(
    private val jpaRepository: JpaRefreshTokenRepository,
) : RefreshTokenRepository {
    override fun findByTokenHash(tokenHash: String): RefreshToken? = jpaRepository.findByTokenHash(tokenHash).orElse(null)

    override fun save(token: RefreshToken): RefreshToken = jpaRepository.save(token)

    override fun deleteAllByUserId(userId: Long) = jpaRepository.deleteAllByUserId(userId)
}
