package com.example.lifelog.application.insight

import com.example.lifelog.common.pagination.CursorCodec
import com.example.lifelog.common.pagination.CursorPage
import com.example.lifelog.common.pagination.CursorPagination
import com.example.lifelog.common.time.TimeZoneConfig
import com.example.lifelog.domain.insight.InsightRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 인사이트 목록 조회 Use Case
 */
@Service
class ListInsightsUseCase(
    private val insightRepository: InsightRepository,
    private val timeZoneConfig: TimeZoneConfig,
) {
    @Transactional(readOnly = true)
    fun execute(
        userId: Long,
        limit: Int,
        cursor: String?,
    ): CursorPage<InsightListItem> {
        val safeLimit = limit.coerceIn(1, 50)
        val pageable = CursorPagination.createPageable(safeLimit)

        val (cursorCreatedAt, cursorId) = CursorCodec.decode(cursor)

        val rows =
            if (cursorCreatedAt == null || cursorId == null) {
                insightRepository.findFirstPage(userId = userId, pageable = pageable)
            } else {
                insightRepository.findNextPage(
                    userId = userId,
                    cursorCreatedAt = cursorCreatedAt,
                    cursorId = cursorId,
                    pageable = pageable,
                )
            }

        val zoneId = timeZoneConfig.defaultZoneId

        val items =
            rows.map { insight ->
                InsightListItem.from(insight, zoneId)
            }

        return CursorPagination.paginate(
            rows = items,
            limit = safeLimit,
            encodeCursor = { item -> CursorCodec.encode(item.createdAt, item.id) },
        )
    }
}
