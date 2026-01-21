package com.example.lifelog.presentation.api.signal

import com.example.lifelog.application.signal.GetSignalObjectsUseCase
import java.time.Instant

data class SignalObjectsResponse(
    val serverTime: Instant,
    val totalCandyCount: Long,
    val activeKeywords: List<ActiveKeywordDto>,
    val waterDrops: List<WaterDropDto>,
) {
    data class ActiveKeywordDto(
        val keywordKey: String,
        val insightText: String?,
        val candyCount: Long,
        val updatedAt: Instant,
    )

    data class WaterDropDto(
        val keywordKey: String,
        val createdAt: Instant,
        val updatedAt: Instant,
        val snapshotText: String?,
    )

    companion object {
        fun from(result: GetSignalObjectsUseCase.Result): SignalObjectsResponse =
            SignalObjectsResponse(
                serverTime = result.serverTime,
                totalCandyCount = result.totalCandyCount,
                activeKeywords =
                    result.activeKeywords.map {
                        ActiveKeywordDto(
                            keywordKey = it.keywordKey,
                            insightText = it.insightText,
                            candyCount = it.candyCount,
                            updatedAt = it.updatedAt,
                        )
                    },
                waterDrops =
                    result.waterDrops.map {
                        WaterDropDto(
                            keywordKey = it.keywordKey,
                            createdAt = it.createdAt,
                            updatedAt = it.updatedAt,
                            snapshotText = it.snapshotText,
                        )
                    },
            )
    }
}
