package com.example.lifelog.infrastructure.persistence.insight

import com.example.lifelog.domain.insight.Insight
import com.example.lifelog.domain.insight.InsightRepository
import org.springframework.stereotype.Component

/**
 * InsightRepository의 JPA 구현을 위한 어댑터
 */
@Component
class InsightRepositoryAdapter(
    private val jpaRepository: JpaInsightRepository,
) : InsightRepository {
    override fun save(insight: Insight): Insight = jpaRepository.save(insight)

    override fun findById(id: Long): Insight? = jpaRepository.findById(id).orElse(null)

    override fun findLatestByUserId(
        userId: Long,
        pageable: org.springframework.data.domain.Pageable,
    ): List<Insight> = jpaRepository.findLatestByUserId(userId, pageable)

    override fun findFirstPage(
        userId: Long,
        pageable: org.springframework.data.domain.Pageable,
    ): List<Insight> = jpaRepository.findFirstPage(userId, pageable)

    override fun findNextPage(
        userId: Long,
        cursorCreatedAt: java.time.Instant,
        cursorId: Long,
        pageable: org.springframework.data.domain.Pageable,
    ): List<Insight> = jpaRepository.findNextPage(userId, cursorCreatedAt, cursorId, pageable)
}
