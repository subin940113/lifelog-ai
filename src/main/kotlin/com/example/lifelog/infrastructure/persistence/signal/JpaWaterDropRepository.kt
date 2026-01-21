package com.example.lifelog.infrastructure.persistence.signal

import com.example.lifelog.domain.signal.WaterDrop
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaWaterDropRepository : JpaRepository<WaterDrop, Long> {
    fun findByUserIdAndKeywordKey(
        userId: Long,
        keywordKey: String,
    ): WaterDrop?

    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<WaterDrop>
}
