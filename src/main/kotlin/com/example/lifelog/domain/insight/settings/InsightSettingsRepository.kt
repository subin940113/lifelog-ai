package com.example.lifelog.domain.insight.settings

/**
 * 인사이트 설정 도메인 리포지토리 인터페이스
 */
interface InsightSettingsRepository {
    fun findByUserId(userId: Long): InsightSettings?

    fun save(settings: InsightSettings): InsightSettings
}
