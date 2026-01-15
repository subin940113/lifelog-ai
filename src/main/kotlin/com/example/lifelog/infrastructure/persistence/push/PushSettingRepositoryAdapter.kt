package com.example.lifelog.infrastructure.persistence.push

import com.example.lifelog.domain.push.PushSetting
import com.example.lifelog.domain.push.PushSettingRepository
import org.springframework.stereotype.Component
import java.util.Optional

/**
 * PushSettingRepository JPA 어댑터
 */
@Component
class PushSettingRepositoryAdapter(
    private val jpaRepo: JpaPushSettingRepository,
) : PushSettingRepository {
    override fun findById(userId: Long): PushSetting? = jpaRepo.findById(userId).orElse(null)

    override fun save(setting: PushSetting): PushSetting = jpaRepo.save(setting)
}
