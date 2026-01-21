package com.example.lifelog.domain.insight

import org.springframework.data.domain.Pageable

interface TopInsightQueryRepository {
    fun findTopLikedInsightsByUserAndKeyword(
        userId: Long,
        keywordKey: String,
        pageable: Pageable,
    ): List<TopInsightRow>
}
