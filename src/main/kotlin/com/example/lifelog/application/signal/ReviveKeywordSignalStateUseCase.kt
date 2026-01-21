package com.example.lifelog.application.signal

import com.example.lifelog.domain.signal.KeywordSignalState
import com.example.lifelog.domain.signal.KeywordSignalStateRepository
import com.example.lifelog.domain.signal.WaterDropRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * 정책:
 * - 키워드가 다시 생성되면(FROZEN → ACTIVE)
 * - 해당 WaterDrop(영구 물방울)은 삭제
 */
@Service
class ReviveKeywordSignalStateUseCase(
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

        // frozen이면 active로 복구, 없으면 신규 active 생성
        state.revive(now)
        keywordSignalStateRepository.save(state)

        // 영구 물방울 삭제(권장 정책)
        val drop = waterDropRepository.findByUserIdAndKeywordKey(userId, kk)
        if (drop != null) {
            waterDropRepository.delete(drop)
        }
    }

    private fun normalizeKey(keywordKey: String): String = keywordKey.trim().lowercase()
}
