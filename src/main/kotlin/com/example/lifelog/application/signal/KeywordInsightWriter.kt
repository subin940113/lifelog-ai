package com.example.lifelog.application.signal

import com.example.lifelog.domain.signal.KeywordSignalStateRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Component
class KeywordInsightWriter(
    private val stateRepository: KeywordSignalStateRepository,
) {
    @Transactional
    fun write(
        userId: Long,
        keywordKey: String,
        insightText: String,
    ) {
        val state = stateRepository.findByUserIdAndKeywordKey(userId, keywordKey) ?: return
        state.applyInsight(text = insightText, now = Instant.now())
        stateRepository.save(state)
    }
}
