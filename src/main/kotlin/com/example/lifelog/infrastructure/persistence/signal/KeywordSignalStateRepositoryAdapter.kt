package com.example.lifelog.infrastructure.persistence.signal

import com.example.lifelog.domain.signal.KeywordSignalState
import com.example.lifelog.domain.signal.KeywordSignalStateRepository
import com.example.lifelog.domain.signal.KeywordSignalStatus
import org.springframework.stereotype.Component

@Component
class KeywordSignalStateRepositoryAdapter(
    private val jpaRepository: JpaKeywordSignalStateRepository,
) : KeywordSignalStateRepository {
    override fun save(state: KeywordSignalState): KeywordSignalState = jpaRepository.save(state)

    override fun findByUserIdAndKeywordKey(
        userId: Long,
        keywordKey: String,
    ): KeywordSignalState? = jpaRepository.findByUserIdAndKeywordKey(userId, keywordKey)

    override fun findActiveByUserId(userId: Long): List<KeywordSignalState> =
        jpaRepository.findByUserIdAndStatusOrderByUpdatedAtDesc(userId, KeywordSignalStatus.ACTIVE)

    override fun findFrozenByUserId(userId: Long): List<KeywordSignalState> =
        jpaRepository.findByUserIdAndStatusOrderByUpdatedAtDesc(userId, KeywordSignalStatus.FROZEN)

    override fun delete(state: KeywordSignalState) {
        jpaRepository.delete(state)
    }
}
