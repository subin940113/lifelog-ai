package com.example.lifelog.log.raw

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class CreateLogRequest(
    @field:NotBlank(message = "content는 비어있을 수 없습니다.")
    @field:Size(max = 20000, message = "content가 너무 깁니다.")
    val content: String,
)

data class CreateLogResponse(
    val logId: Long,
)

data class LogsPageResponse(
    val items: List<LogListItem>,
    val nextCursor: String?,
)

data class LogListItem(
    val logId: Long,
    val createdAt: String, // ISO-8601 string
    val dateLabel: String, // e.g. "2026.01.05"
    val timeLabel: String, // e.g. "19:02"
    val preview: String,
) {
    companion object {
        private val dateFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
        private val timeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        fun from(
            row: RawLog,
            zone: ZoneId,
        ): LogListItem {
            val zdt = row.createdAt.atZone(zone)
            val dateLabel = zdt.format(dateFmt)
            val timeLabel = zdt.format(timeFmt)

            return LogListItem(
                logId = row.id,
                createdAt = row.createdAt.toString(),
                dateLabel = dateLabel,
                timeLabel = timeLabel,
                preview = toPreview(row.content),
            )
        }

        private fun toPreview(content: String): String {
            val singleLine = content.replace("\n", " ").trim()
            val max = 140
            return if (singleLine.length <= max) singleLine else singleLine.substring(0, max).trimEnd() + "…"
        }
    }
}
