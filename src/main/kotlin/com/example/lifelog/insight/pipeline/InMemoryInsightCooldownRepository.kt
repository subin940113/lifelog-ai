package com.example.lifelog.insight.pipeline

import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap

@Component
class InMemoryInsightCooldownRepository : InsightCooldownRepository {
    private val lastRun = ConcurrentHashMap<Long, Instant>()
    private val dailyCount = ConcurrentHashMap<Long, MutableList<Instant>>() // run timestamps

    private val zone = ZoneId.of("Asia/Seoul")

    override fun getLastRunAt(userId: Long): Instant? = lastRun[userId]

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

    override fun getTodayCount(
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
