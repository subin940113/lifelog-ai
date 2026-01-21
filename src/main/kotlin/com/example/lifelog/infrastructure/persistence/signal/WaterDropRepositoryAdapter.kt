package com.example.lifelog.infrastructure.persistence.signal

import com.example.lifelog.domain.signal.WaterDrop
import com.example.lifelog.domain.signal.WaterDropRepository
import org.springframework.stereotype.Component

@Component
class WaterDropRepositoryAdapter(
    private val jpaRepository: JpaWaterDropRepository,
) : WaterDropRepository {
    override fun save(drop: WaterDrop): WaterDrop = jpaRepository.save(drop)

    override fun findByUserIdAndKeywordKey(
        userId: Long,
        keywordKey: String,
    ): WaterDrop? = jpaRepository.findByUserIdAndKeywordKey(userId, keywordKey)

    override fun findByUserId(userId: Long): List<WaterDrop> = jpaRepository.findByUserIdOrderByCreatedAtDesc(userId)

    override fun delete(drop: WaterDrop) {
        jpaRepository.delete(drop)
    }
}
