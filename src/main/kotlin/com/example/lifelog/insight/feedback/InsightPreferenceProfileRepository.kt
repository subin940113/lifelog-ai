package com.example.lifelog.insight.feedback

import org.springframework.data.jpa.repository.JpaRepository

interface InsightPreferenceProfileRepository : JpaRepository<InsightPreferenceProfile, Long> {
    fun findByUserId(userId: Long): InsightPreferenceProfile?
}
