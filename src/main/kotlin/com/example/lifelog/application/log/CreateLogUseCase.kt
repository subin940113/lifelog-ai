package com.example.lifelog.application.log

import com.example.lifelog.common.pagination.CursorCodec
import com.example.lifelog.common.pagination.CursorPage
import com.example.lifelog.common.pagination.CursorPagination
import com.example.lifelog.common.time.DateTimeFormatters
import com.example.lifelog.common.time.TimeZoneConfig
import com.example.lifelog.domain.log.LogRepository
import com.example.lifelog.domain.log.RawLog
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 로그 생성 Use Case
 */
@Service
class CreateLogUseCase(
    private val logRepository: LogRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    @Transactional
    fun execute(userId: Long, content: String): RawLog {
        val trimmed = content.trim()
        require(trimmed.isNotEmpty()) { "content is blank" }

        val log = RawLog(
            userId = userId,
            content = trimmed,
        )

        val saved = logRepository.save(log)

        // 트랜잭션 커밋 이후 리스너에서 받도록 이벤트 발행
        eventPublisher.publishEvent(com.example.lifelog.domain.log.RawLogCreatedEvent(saved))

        return saved
    }
}

/**
 * 로그 목록 조회 Use Case
 */
@Service
class ListLogsUseCase(
    private val logRepository: LogRepository,
    private val timeZoneConfig: TimeZoneConfig,
) {
    @Transactional(readOnly = true)
    fun execute(
        userId: Long,
        limit: Int,
        cursor: String?,
    ): CursorPage<LogListItem> {
        val safeLimit = limit.coerceIn(1, 100)
        val pageable = CursorPagination.createPageable(safeLimit)

        val (cursorCreatedAt, cursorId) = CursorCodec.decode(cursor)

        val rows =
            if (cursorCreatedAt == null || cursorId == null) {
                logRepository.findFirstPage(userId = userId, pageable = pageable)
            } else {
                logRepository.findNextPage(
                    userId = userId,
                    cursorCreatedAt = cursorCreatedAt,
                    cursorId = cursorId,
                    pageable = pageable,
                )
            }

        val zoneId = timeZoneConfig.defaultZoneId

        val items = rows.map { log ->
            LogListItem.from(log, zoneId)
        }

        return CursorPagination.paginate(
            rows = items,
            limit = safeLimit,
            encodeCursor = { item -> CursorCodec.encode(item.createdAt, item.logId) },
        )
    }
}

/**
 * 로그 목록 아이템 DTO
 */
data class LogListItem(
    val logId: Long,
    val createdAt: java.time.Instant,
    val createdAtLabel: String, // ISO-8601 string
    val dateLabel: String, // e.g. "2026.01.05"
    val timeLabel: String, // e.g. "19:02"
    val preview: String,
) {
    companion object {
        fun from(
            log: RawLog,
            zoneId: java.time.ZoneId,
        ): LogListItem {
            val createdAt = log.createdAt
            val zdt = createdAt.atZone(zoneId)

            return LogListItem(
                logId = log.id,
                createdAt = createdAt,
                createdAtLabel = createdAt.toString(),
                dateLabel = DateTimeFormatters.formatDate(createdAt, zoneId),
                timeLabel = DateTimeFormatters.formatTime(createdAt, zoneId),
                preview = log.preview(),
            )
        }
    }
}
