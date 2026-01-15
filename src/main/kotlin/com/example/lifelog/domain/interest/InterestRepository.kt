package com.example.lifelog.domain.interest

/**
 * Interest 도메인 리포지토리 인터페이스
 */
interface InterestRepository {
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
    fun save(keyword: InterestKeyword): InterestKeyword
    fun delete(keyword: InterestKeyword)
}
