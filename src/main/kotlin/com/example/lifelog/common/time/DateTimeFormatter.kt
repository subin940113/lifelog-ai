package com.example.lifelog.common.time

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * 공통 날짜/시간 포맷터
 */
object DateTimeFormatters {
    val DATE_LABEL: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    val TIME_LABEL: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val DATETIME_LABEL: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    /**
     * Instant를 특정 ZoneId로 변환하여 포맷팅
     */
    fun formatDate(instant: java.time.Instant, zoneId: ZoneId): String {
        return instant.atZone(zoneId).format(DATE_LABEL)
    }

    fun formatTime(instant: java.time.Instant, zoneId: ZoneId): String {
        return instant.atZone(zoneId).format(TIME_LABEL)
    }

    fun formatDateTime(instant: java.time.Instant, zoneId: ZoneId): String {
        return instant.atZone(zoneId).format(DATETIME_LABEL)
    }
}
