package com.example.lifelog.domain.signal

interface KeywordSignalStateRepository {
    fun save(state: KeywordSignalState): KeywordSignalState

    fun findByUserIdAndKeywordKey(
        userId: Long,
        keywordKey: String,
    ): KeywordSignalState?

    fun findActiveByUserId(userId: Long): List<KeywordSignalState>

    fun findFrozenByUserId(userId: Long): List<KeywordSignalState>

    fun delete(state: KeywordSignalState)
}
