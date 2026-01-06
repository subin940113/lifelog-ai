package com.example.lifelog.insight.pipeline

import java.time.Instant

interface InsightCooldownRepository {
    fun getLastRunAt(userId: Long): Instant?

    fun markRun(
        userId: Long,
        at: Instant,
    )

    fun getTodayCount(
        userId: Long,
        now: Instant,
    ): Int
}
