package com.example.lifelog.infrastructure.persistence.signal

import com.example.lifelog.domain.signal.KeywordSignalState
import com.example.lifelog.domain.signal.KeywordSignalStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaKeywordSignalStateRepository : JpaRepository<KeywordSignalState, Long> {
    fun findByUserIdAndKeywordKey(
        userId: Long,
        keywordKey: String,
    ): KeywordSignalState?

    fun findByUserIdAndStatusOrderByUpdatedAtDesc(
        userId: Long,
        status: KeywordSignalStatus,
    ): List<KeywordSignalState>
}
