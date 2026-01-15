package com.example.lifelog.domain.insight

import java.time.Instant

/**
 * 인사이트 쿨다운 도메인 리포지토리 인터페이스
 * 구현체는 infrastructure 레이어에 위치
 */
interface InsightCooldownRepository {
    fun findLastRunAt(userId: Long): Instant?

    fun markRun(
        userId: Long,
        at: Instant,
    )

    fun countToday(
        userId: Long,
        now: Instant,
    ): Int
}
