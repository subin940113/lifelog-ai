package com.example.lifelog.infrastructure.persistence.interest

import com.example.lifelog.domain.interest.InterestKeyword
import com.example.lifelog.domain.interest.InterestRepository
import org.springframework.stereotype.Component

/**
 * InterestRepository JPA 어댑터
 */
@Component
class InterestRepositoryAdapter(
    private val jpaRepository: JpaInterestKeywordRepository,
) : InterestRepository {
    override fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): List<InterestKeyword> =
        jpaRepository.findAllByUserIdOrderByCreatedAtDesc(userId)

    override fun countByUserId(userId: Long): Long = jpaRepository.countByUserId(userId)

    override fun existsByUserIdAndKeywordKey(
        userId: Long,
        keywordKey: String,
    ): Boolean = jpaRepository.existsByUserIdAndKeywordKey(userId, keywordKey)

    override fun findByUserIdAndKeywordKey(
        userId: Long,
        keywordKey: String,
    ): InterestKeyword? = jpaRepository.findByUserIdAndKeywordKey(userId, keywordKey)

    override fun save(keyword: InterestKeyword): InterestKeyword = jpaRepository.save(keyword)

    override fun delete(keyword: InterestKeyword) {
        jpaRepository.delete(keyword)
    }
}
