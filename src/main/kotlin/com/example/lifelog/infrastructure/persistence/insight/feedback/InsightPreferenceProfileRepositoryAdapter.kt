package com.example.lifelog.infrastructure.persistence.insight.feedback

import com.example.lifelog.domain.insight.feedback.InsightPreferenceProfile
import com.example.lifelog.domain.insight.feedback.InsightPreferenceProfileRepository
import org.springframework.stereotype.Component

/**
 * InsightPreferenceProfileRepository JPA 어댑터
 */
@Component
class InsightPreferenceProfileRepositoryAdapter(
    private val jpaRepository: JpaInsightPreferenceProfileRepository,
) : InsightPreferenceProfileRepository {
    override fun findByUserId(userId: Long): InsightPreferenceProfile? = jpaRepository.findByUserId(userId)

    override fun save(profile: InsightPreferenceProfile): InsightPreferenceProfile = jpaRepository.save(profile)
}
