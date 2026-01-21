package com.example.lifelog.application.signal

import com.example.lifelog.domain.signal.KeywordSignalState
import com.example.lifelog.domain.signal.KeywordSignalStateRepository
import com.example.lifelog.domain.signal.WaterDrop
import com.example.lifelog.domain.signal.WaterDropRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * 정책:
 * - 키워드 삭제 시 KeywordSignalState를 FROZEN으로 전환
 * - WaterDrop(영구 물방울)을 생성(없으면 생성 / 있으면 그대로)
 */
@Service
class FreezeKeywordSignalStateUseCase(
    private val keywordSignalStateRepository: KeywordSignalStateRepository,
    private val waterDropRepository: WaterDropRepository,
) {
    @Transactional
    fun execute(
        userId: Long,
        keywordKey: String,
        now: Instant = Instant.now(),
    ) {
        val kk = normalizeKey(keywordKey)
        if (kk.isBlank()) return

        val state =
            keywordSignalStateRepository.findByUserIdAndKeywordKey(userId, kk)
                ?: KeywordSignalState(
                    userId = userId,
                    keywordKey = kk,
                )

        state.freeze(now)
        keywordSignalStateRepository.save(state)

        // WaterDrop은 "키워드 삭제로 더이상 발전 불가" 시점에 생성됨
        val existingDrop = waterDropRepository.findByUserIdAndKeywordKey(userId, kk)
        if (existingDrop == null) {
            waterDropRepository.save(
                WaterDrop(
                    userId = userId,
                    keywordKey = kk,
                    createdAt = now,
                    updatedAt = now,
                    snapshotText = state.insightText,
                ),
            )
        }
    }

    private fun normalizeKey(keywordKey: String): String = keywordKey.trim().lowercase()
}
