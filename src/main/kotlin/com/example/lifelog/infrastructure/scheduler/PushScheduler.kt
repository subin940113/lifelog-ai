package com.example.lifelog.infrastructure.scheduler

import com.example.lifelog.application.push.OrchestratePushUseCase
import com.example.lifelog.domain.push.PushTokenRepository
import com.example.lifelog.infrastructure.config.PushPolicyProperties
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 푸시 발송 스케줄러
 */
@Component
class PushScheduler(
    private val properties: PushPolicyProperties,
    private val pushTokenRepository: PushTokenRepository,
    private val orchestratePushUseCase: OrchestratePushUseCase,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelayString = "10000") // 10분
    fun scanAndSend() {
        log.info("[PUSH] scheduler tick enabled={}", properties.enabled)
        if (!properties.enabled) return

        val userIds = pushTokenRepository.findDistinctEnabledUserIds()
        log.info("[PUSH] target users count={}", userIds.size)
        if (userIds.isEmpty()) return

        userIds.forEach { userId ->
            val result = runCatching { orchestratePushUseCase.execute(userId) }
            result.onFailure { exception -> log.warn("[PUSH] tickUser failed userId={}", userId, exception) }
        }
    }
}
