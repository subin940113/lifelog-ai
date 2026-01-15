package com.example.lifelog.domain.insight.feedback

/**
 * 인사이트 선호 프로필 도메인 리포지토리 인터페이스
 */
interface InsightPreferenceProfileRepository {
    fun findByUserId(userId: Long): InsightPreferenceProfile?
    fun save(profile: InsightPreferenceProfile): InsightPreferenceProfile
}
