package com.example.lifelog.infrastructure.persistence.insight.feedback

import com.example.lifelog.domain.insight.feedback.InsightPreferenceProfile
import org.springframework.data.jpa.repository.JpaRepository

/**
 * InsightPreferenceProfile JPA Repository
 */
interface JpaInsightPreferenceProfileRepository : JpaRepository<InsightPreferenceProfile, Long> {
    fun findByUserId(userId: Long): InsightPreferenceProfile?
}
