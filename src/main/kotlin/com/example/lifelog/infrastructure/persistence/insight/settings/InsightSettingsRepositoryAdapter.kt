package com.example.lifelog.infrastructure.persistence.insight.settings

import com.example.lifelog.domain.insight.settings.InsightSettings
import com.example.lifelog.domain.insight.settings.InsightSettingsRepository
import org.springframework.stereotype.Component

/**
 * InsightSettingsRepository JPA 어댑터
 */
@Component
class InsightSettingsRepositoryAdapter(
    private val jpaRepository: JpaInsightSettingsRepository,
) : InsightSettingsRepository {
    override fun findByUserId(userId: Long): InsightSettings? = jpaRepository.findByUserId(userId)

    override fun save(settings: InsightSettings): InsightSettings = jpaRepository.save(settings)
}
