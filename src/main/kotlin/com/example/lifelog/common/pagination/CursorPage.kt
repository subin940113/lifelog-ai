package com.example.lifelog.common.pagination

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

/**
 * 커서 기반 페이지네이션 결과
 */
data class CursorPage<T>(
    val items: List<T>,
    val nextCursor: String?,
    val hasMore: Boolean,
)

/**
 * 커서 기반 페이지네이션을 위한 공통 로직
 */
object CursorPagination {
    /**
     * 커서 기반 페이지네이션 처리
     * @param rows 전체 조회된 데이터 (limit + 1개)
     * @param limit 요청한 limit
     * @param encodeCursor 마지막 아이템의 커서를 생성하는 함수
     */
    fun <T> paginate(
        rows: List<T>,
        limit: Int,
        encodeCursor: (T) -> String,
    ): CursorPage<T> {
        val safeLimit = limit.coerceIn(1, 100)
        val hasMore = rows.size > safeLimit
        val slice = if (hasMore) rows.take(safeLimit) else rows

        val nextCursor =
            if (!hasMore || slice.isEmpty()) {
                null
            } else {
                val last = slice.last()
                encodeCursor(last)
            }

        return CursorPage(
            items = slice,
            nextCursor = nextCursor,
            hasMore = hasMore,
        )
    }

    /**
     * 커서 기반 페이지네이션을 위한 Pageable 생성
     */
    fun createPageable(limit: Int): Pageable {
        val safeLimit = limit.coerceIn(1, 100)
        return PageRequest.of(0, safeLimit + 1) // hasMore 체크를 위해 +1
    }
}
