package com.example.lifelog.insight.settings

import org.springframework.data.jpa.repository.JpaRepository

interface InsightSettingsRepository : JpaRepository<InsightSettings, Long> {
    fun findByUserId(userId: Long): InsightSettings?
}
