package com.example.lifelog.domain.insight

import org.springframework.data.domain.Pageable
import java.time.Instant

/**
 * Insight 도메인 리포지토리 인터페이스
 * 구현체는 infrastructure 레이어에 위치
 */
interface InsightRepository {
    fun save(insight: Insight): Insight

    fun findById(id: Long): Insight?

    fun findLatestByUserId(
        userId: Long,
        pageable: Pageable,
    ): List<Insight>

    fun findFirstPage(
        userId: Long,
        pageable: Pageable,
    ): List<Insight>

    fun findNextPage(
        userId: Long,
        cursorCreatedAt: Instant,
        cursorId: Long,
        pageable: Pageable,
    ): List<Insight>
}
