package com.example.lifelog.interest

import org.springframework.data.jpa.repository.JpaRepository

interface InterestKeywordRepository : JpaRepository<InterestKeyword, Long> {
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
