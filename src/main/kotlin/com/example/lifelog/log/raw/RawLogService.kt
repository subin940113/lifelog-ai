package com.example.lifelog.log.raw

import com.example.lifelog.log.event.RawLogCreatedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId

@Service
class RawLogService(
    private val repo: RawLogRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    private val seoul: ZoneId = ZoneId.of("Asia/Seoul")

    @Transactional
    fun create(
        userId: Long,
        content: String,
    ): RawLog {
        val trimmed = content.trim()
        require(trimmed.isNotEmpty()) { "content is blank" }

        val saved =
            repo.save(
                RawLog(
                    userId = userId,
                    content = trimmed,
                ),
            )

        // 트랜잭션 커밋 이후 리스너에서 받도록 이벤트 발행
        eventPublisher.publishEvent(RawLogCreatedEvent(saved))

        return saved
    }

    @Transactional(readOnly = true)
    fun list(
        userId: Long,
        limit: Int,
        cursor: String?,
    ): LogsPageResponse {
        val safeLimit = limit.coerceIn(1, 100)
        val page = PageRequest.of(0, safeLimit + 1)

        val (cursorCreatedAt, cursorId) = CursorCodec.decode(cursor)

        val rows =
            if (cursorCreatedAt == null || cursorId == null) {
                repo.findFirstPage(userId = userId, pageable = page)
            } else {
                repo.findNextPage(
                    userId = userId,
                    cursorCreatedAt = cursorCreatedAt,
                    cursorId = cursorId,
                    pageable = page,
                )
            }

        val hasMore = rows.size > safeLimit
        val slice = if (hasMore) rows.take(safeLimit) else rows

        val items = slice.map { LogListItem.from(it, seoul) }

        val nextCursor =
            if (!hasMore || slice.isEmpty()) {
                null
            } else {
                val last = slice.last()
                CursorCodec.encode(last.createdAt, last.id)
            }

        return LogsPageResponse(items = items, nextCursor = nextCursor)
    }
}
