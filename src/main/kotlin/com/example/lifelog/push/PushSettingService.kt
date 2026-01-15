package com.example.lifelog.push

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class PushSettingService(
    private val repo: PushSettingRepository,
    private val tokenRepo: PushTokenRepository, // 이미 존재
) {
    @Transactional(readOnly = true)
    fun get(userId: Long): PushSetting {
        // 없으면 "기본 ON"으로 간주
        return repo.findById(userId).orElse(PushSetting(userId = userId, enabled = true))
    }

    @Transactional
    fun setEnabled(
        userId: Long,
        enabled: Boolean,
    ): PushSetting {
        val s = repo.findById(userId).orElse(PushSetting(userId = userId, enabled = true))
        s.enabled = enabled
        s.updatedAt = Instant.now()
        val saved = repo.save(s)

        // 유저가 OFF 하면 현재 저장된 모든 토큰도 즉시 비활성화(불필요 발송 방지)
        if (!enabled) {
            val tokens = tokenRepo.findAllByUserIdAndEnabledTrue(userId) // 없으면 추가 필요(아래 참고)
            if (tokens.isNotEmpty()) {
                tokens.forEach { it.enabled = false }
                tokenRepo.saveAll(tokens)
            }
        }

        return saved
    }
}
