package com.example.lifelog.infrastructure.cache

import com.example.lifelog.domain.insight.InsightCooldownRepository
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap

/**
 * 인메모리 인사이트 쿨다운 리포지토리 구현체
 */
@Component
class InMemoryInsightCooldownRepository : InsightCooldownRepository {
    private val lastRun = ConcurrentHashMap<Long, Instant>()
    private val dailyCount = ConcurrentHashMap<Long, MutableList<Instant>>() // run timestamps

    private val zone = ZoneId.of("Asia/Seoul")

    override fun findLastRunAt(userId: Long): Instant? = lastRun[userId]

    override fun markRun(
        userId: Long,
        at: Instant,
    ) {
        lastRun[userId] = at
        dailyCount.compute(userId) { _, list ->
            val l = list ?: mutableListOf()
            l.add(at)
            l
        }
    }

    override fun countToday(
        userId: Long,
        now: Instant,
    ): Int {
        val list = dailyCount[userId] ?: return 0

        val today = ZonedDateTime.ofInstant(now, zone).toLocalDate()
        val filtered = list.filter { ZonedDateTime.ofInstant(it, zone).toLocalDate().isEqual(today) }

        // 메모리 관리: 오늘 것만 유지
        dailyCount[userId] = filtered.toMutableList()
        return filtered.size
    }
}
