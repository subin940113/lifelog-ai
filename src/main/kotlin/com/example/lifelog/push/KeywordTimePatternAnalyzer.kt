package com.example.lifelog.push

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class KeywordTimePatternAnalyzer(
    private val zone: ZoneId,
    private val bucketMinutes: Int,
) {
    data class Pattern(
        val bucketStartMinute: Int, // 0~1439
        val occurrences: Int,
    )

    fun detectMostFrequentBucket(
        rows: List<Pair<Instant, String>>,
        keyword: String,
    ): Pattern? {
        val kw = keyword.trim()
        if (kw.isEmpty()) return null

        val hits =
            rows
                .asSequence()
                .filter { (_, content) -> content.contains(kw, ignoreCase = true) }
                .map { (createdAt, _) ->
                    val z = ZonedDateTime.ofInstant(createdAt, zone)
                    val minuteOfDay = z.hour * 60 + z.minute
                    val bucketStart = (minuteOfDay / bucketMinutes) * bucketMinutes
                    bucketStart
                }.toList()

        if (hits.isEmpty()) return null

        val freq = hits.groupingBy { it }.eachCount()
        val best = freq.maxByOrNull { it.value } ?: return null

        return Pattern(
            bucketStartMinute = best.key,
            occurrences = best.value,
        )
    }
}
