package com.example.lifelog.infrastructure.persistence.push

import com.example.lifelog.domain.push.PushToken
import com.example.lifelog.domain.push.PushTokenRepository
import org.springframework.stereotype.Component

/**
 * PushTokenRepository JPA 어댑터
 */
@Component
class PushTokenRepositoryAdapter(
    private val jpaRepo: JpaPushTokenRepository,
) : PushTokenRepository {
    override fun findByUserIdAndToken(
        userId: Long,
        token: String,
    ): PushToken? = jpaRepo.findByUserIdAndToken(userId, token)

    override fun findByUserIdAndEnabledTrue(userId: Long): List<PushToken> =
        jpaRepo.findByUserIdAndEnabledTrue(userId)

    override fun findEnabledByUserId(userId: Long): List<PushToken> =
        jpaRepo.findEnabledByUserId(userId)

    override fun save(token: PushToken): PushToken = jpaRepo.save(token)

    override fun saveAll(tokens: List<PushToken>): List<PushToken> = jpaRepo.saveAll(tokens)

    override fun deleteByUserIdAndToken(
        userId: Long,
        token: String,
    ): Long = jpaRepo.deleteByUserIdAndToken(userId, token)

    override fun findDistinctEnabledUserIds(): List<Long> = jpaRepo.findDistinctEnabledUserIds()

    override fun findAllByUserIdAndEnabledTrue(userId: Long): List<PushToken> =
        jpaRepo.findAllByUserIdAndEnabledTrue(userId)

    override fun deleteByToken(token: String) = jpaRepo.deleteByToken(token)
}
