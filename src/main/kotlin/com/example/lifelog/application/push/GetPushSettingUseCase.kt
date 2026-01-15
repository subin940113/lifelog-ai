package com.example.lifelog.application.push

import com.example.lifelog.domain.push.PushSetting
import com.example.lifelog.domain.push.PushSettingRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 푸시 설정 조회 Use Case
 */
@Service
class GetPushSettingUseCase(
    private val pushSettingRepository: PushSettingRepository,
) {
    @Transactional(readOnly = true)
    fun execute(userId: Long): PushSetting {
        // 없으면 "기본 ON"으로 간주
        return pushSettingRepository.findById(userId)
            ?: PushSetting(userId = userId, enabled = true)
    }
}
