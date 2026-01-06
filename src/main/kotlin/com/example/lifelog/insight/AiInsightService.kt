package com.example.lifelog.insight

import com.example.lifelog.log.raw.CursorCodec
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId

@Service
class AiInsightService(
    private val repo: AiInsightRepository,
) {
    private val seoul: ZoneId = ZoneId.of("Asia/Seoul")

    @Transactional(readOnly = true)
    fun list(
        userId: Long,
        limit: Int,
        cursor: String?,
    ): AiInsightsPageResponse {
        val safeLimit = limit.coerceIn(1, 50)
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

        val items = slice.map { AiInsightListItem.from(it, seoul) }

        val nextCursor =
            if (!hasMore || slice.isEmpty()) {
                null
            } else {
                val last = slice.last()
                CursorCodec.encode(last.createdAt, last.id)
            }

        return AiInsightsPageResponse(
            insights = items,
            nextCursor = nextCursor,
        )
    }
}
