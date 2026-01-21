package com.example.lifelog.application.signal

import com.example.lifelog.domain.log.LogRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Component
class KeywordInsightLogCollector(
    private val logRepository: LogRepository,
) {
    fun collect(
        userId: Long,
        keywordKey: String,
        limit: Int = 50,
    ): List<String> =
        logRepository
            .findLatestByUserId(userId, PageRequest.of(0, limit))
            .asSequence()
            .filter { it.content.lowercase().contains(keywordKey) }
            .map { it.content }
            .toList()
}
