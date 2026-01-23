package com.example.lifelog.infrastructure.persistence.auth

import com.example.lifelog.domain.auth.AppleRefreshToken
import com.example.lifelog.domain.auth.AppleRefreshTokenRepository
import org.springframework.stereotype.Component

/**
 * AppleRefreshTokenRepository JPA 어댑터
 */
@Component
class AppleRefreshTokenRepositoryAdapter(
    private val jpaRepository: JpaAppleRefreshTokenRepository,
) : AppleRefreshTokenRepository {
    override fun findByUserId(userId: Long): AppleRefreshToken? = jpaRepository.findByUserId(userId)

    override fun save(token: AppleRefreshToken): AppleRefreshToken = jpaRepository.save(token)

    override fun deleteByUserId(userId: Long) = jpaRepository.deleteByUserId(userId)
}
