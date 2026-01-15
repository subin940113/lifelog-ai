package com.example.lifelog.infrastructure.persistence.interest

import com.example.lifelog.domain.interest.InterestKeyword
import org.springframework.data.jpa.repository.JpaRepository

/**
 * InterestKeyword JPA Repository
 */
interface JpaInterestKeywordRepository : JpaRepository<InterestKeyword, Long> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): List<InterestKeyword>

    fun countByUserId(userId: Long): Long

    fun existsByUserIdAndKeywordKey(
        userId: Long,
        keywordKey: String,
    ): Boolean

    fun findByUserIdAndKeywordKey(
        userId: Long,
        keywordKey: String,
    ): InterestKeyword?
}
