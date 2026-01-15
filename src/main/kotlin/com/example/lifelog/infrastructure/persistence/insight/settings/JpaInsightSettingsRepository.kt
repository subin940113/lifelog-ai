package com.example.lifelog.infrastructure.persistence.insight.settings

import com.example.lifelog.domain.insight.settings.InsightSettings
import org.springframework.data.jpa.repository.JpaRepository

/**
 * InsightSettings JPA Repository
 */
interface JpaInsightSettingsRepository : JpaRepository<InsightSettings, Long> {
    fun findByUserId(userId: Long): InsightSettings?
}
