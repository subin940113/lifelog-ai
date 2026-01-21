package com.example.lifelog.application.signal

import com.example.lifelog.domain.log.LogRepository
import com.example.lifelog.domain.signal.KeywordSignalStateRepository
import com.example.lifelog.domain.signal.WaterDropRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class GetSignalObjectsUseCase(
    private val keywordSignalStateRepository: KeywordSignalStateRepository,
    private val waterDropRepository: WaterDropRepository,
    private val logRepository: LogRepository,
) {
    data class Result(
        val activeKeywords: List<ActiveKeywordItem>,
        val totalCandyCount: Long,
        val waterDrops: List<WaterDropItem>,
        val serverTime: Instant,
    )

    data class ActiveKeywordItem(
        val keywordKey: String,
        val candyCount: Long,
        val insightText: String?,
        val updatedAt: Instant,
    )

    data class WaterDropItem(
        val keywordKey: String,
        val createdAt: Instant,
        val updatedAt: Instant,
        val snapshotText: String?,
    )

    @Transactional(readOnly = true)
    fun execute(userId: Long): Result {
        val serverTime = Instant.now()

        val active =
            keywordSignalStateRepository
                .findActiveByUserId(userId)
                .map {
                    ActiveKeywordItem(
                        keywordKey = it.keywordKey,
                        candyCount = it.candyCount,
                        insightText = it.insightText,
                        updatedAt = it.updatedAt,
                    )
                }

        val drops =
            waterDropRepository
                .findByUserId(userId)
                .map {
                    WaterDropItem(
                        keywordKey = it.keywordKey,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                        snapshotText = it.snapshotText,
                    )
                }

        val totalCandyCount = logRepository.countByUserId(userId)

        return Result(
            activeKeywords = active,
            totalCandyCount = totalCandyCount,
            waterDrops = drops,
            serverTime = serverTime,
        )
    }
}
