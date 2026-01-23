package com.example.lifelog.infrastructure.persistence.push

import com.example.lifelog.domain.push.PushToken
import com.example.lifelog.domain.push.PushTokenRepository
import org.springframework.stereotype.Component

/**
 * PushTokenRepository JPA 어댑터
 */
@Component
class PushTokenRepositoryAdapter(
    private val jpaRepository: JpaPushTokenRepository,
) : PushTokenRepository {
    override fun findByUserIdAndToken(
        userId: Long,
        token: String,
    ): PushToken? = jpaRepository.findByUserIdAndToken(userId, token)

    override fun findByUserIdAndEnabledTrue(userId: Long): List<PushToken> = jpaRepository.findByUserIdAndEnabledTrue(userId)

    override fun findEnabledByUserId(userId: Long): List<PushToken> = jpaRepository.findEnabledByUserId(userId)

    override fun save(token: PushToken): PushToken = jpaRepository.save(token)

    override fun saveAll(tokens: List<PushToken>): List<PushToken> = jpaRepository.saveAll(tokens)

    override fun deleteByUserIdAndToken(
        userId: Long,
        token: String,
    ): Long = jpaRepository.deleteByUserIdAndToken(userId, token)

    override fun findDistinctEnabledUserIds(): List<Long> = jpaRepository.findDistinctEnabledUserIds()

    override fun findAllByUserIdAndEnabledTrue(userId: Long): List<PushToken> = jpaRepository.findAllByUserIdAndEnabledTrue(userId)

    override fun deleteByToken(token: String) = jpaRepository.deleteByToken(token)
}
