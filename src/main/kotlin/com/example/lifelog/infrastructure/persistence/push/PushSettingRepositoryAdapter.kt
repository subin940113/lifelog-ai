package com.example.lifelog.infrastructure.persistence.push

import com.example.lifelog.domain.push.PushSetting
import com.example.lifelog.domain.push.PushSettingRepository
import org.springframework.stereotype.Component

/**
 * PushSettingRepository JPA 어댑터
 */
@Component
class PushSettingRepositoryAdapter(
    private val jpaRepository: JpaPushSettingRepository,
) : PushSettingRepository {
    override fun findById(userId: Long): PushSetting? = jpaRepository.findById(userId).orElse(null)

    override fun save(setting: PushSetting): PushSetting = jpaRepository.save(setting)
}
