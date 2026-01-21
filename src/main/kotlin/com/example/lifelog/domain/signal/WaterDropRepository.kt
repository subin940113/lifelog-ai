package com.example.lifelog.domain.signal

interface WaterDropRepository {
    fun save(drop: WaterDrop): WaterDrop

    fun findByUserIdAndKeywordKey(
        userId: Long,
        keywordKey: String,
    ): WaterDrop?

    fun findByUserId(userId: Long): List<WaterDrop>

    fun delete(drop: WaterDrop)
}
