package com.example.lifelog.application.push

import com.example.lifelog.common.exception.ErrorCode
import com.example.lifelog.common.exception.ValidationException
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * 시간 패턴 데이터 클래스
 */
data class TimePattern(
    val bucketStartMinute: Int, // 0..1439
    val activeDays: Int,
)

/**
 * 시간 패턴 분석기
 */
class TimePatternAnalyzer(
    private val zone: ZoneId,
    private val bucketMinutes: Int,
) {
    init {
        if (bucketMinutes !in 5..180) {
            throw ValidationException(ErrorCode.VALIDATION_OUT_OF_RANGE, "bucketMinutes out of range: $bucketMinutes")
        }
        if (1440 % bucketMinutes != 0) {
            throw ValidationException(ErrorCode.VALIDATION_INVALID_BUCKET_MINUTES, "bucketMinutes must divide 1440: $bucketMinutes")
        }
    }

    fun detectMostFrequentBucket(logs: List<Pair<Instant, String>>): TimePattern? {
        if (logs.isEmpty()) return null

        // day -> bucket -> count
        val dayBucketSet = mutableSetOf<Pair<String, Int>>() // (yyyy-mm-dd, bucketStartMin)

        logs.forEach { (createdAt, _) ->
            val z = ZonedDateTime.ofInstant(createdAt, zone)
            val dayKey = z.toLocalDate().toString()
            val minuteOfDay = z.hour * 60 + z.minute
            val bucketStart = (minuteOfDay / bucketMinutes) * bucketMinutes
            dayBucketSet.add(dayKey to bucketStart)
        }

        // bucket -> activeDays
        val bucketDays = mutableMapOf<Int, MutableSet<String>>()
        for ((dayKey, bucket) in dayBucketSet) {
            bucketDays.computeIfAbsent(bucket) { mutableSetOf() }.add(dayKey)
        }

        val best =
            bucketDays.entries
                .maxWithOrNull(compareBy<Map.Entry<Int, Set<String>>> { it.value.size }.thenBy { -it.key })

        return best?.let { TimePattern(bucketStartMinute = it.key, activeDays = it.value.size) }
    }
}
