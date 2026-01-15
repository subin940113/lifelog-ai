package com.example.lifelog.application.push

import com.example.lifelog.domain.push.PushSetting
import com.example.lifelog.domain.push.PushSettingRepository
import com.example.lifelog.domain.push.PushTokenRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 푸시 설정 업데이트 Use Case
 */
@Service
class UpdatePushSettingUseCase(
    private val pushSettingRepository: PushSettingRepository,
    private val pushTokenRepository: PushTokenRepository,
) {
    @Transactional
    fun execute(
        userId: Long,
        enabled: Boolean,
    ): PushSetting {
        val setting =
            pushSettingRepository.findById(userId)
                ?: PushSetting(userId = userId, enabled = true)

        if (enabled) {
            setting.enable()
        } else {
            setting.disable()
        }

        val saved = pushSettingRepository.save(setting)

        // 유저가 OFF 하면 현재 저장된 모든 토큰도 즉시 비활성화(불필요 발송 방지)
        if (!enabled) {
            val tokens = pushTokenRepository.findAllByUserIdAndEnabledTrue(userId)
            if (tokens.isNotEmpty()) {
                tokens.forEach { it.disable() }
                pushTokenRepository.saveAll(tokens)
            }
        }

        return saved
    }
}
