package com.example.lifelog.push.scheduler

import com.example.lifelog.push.PushPolicyProperties
import com.example.lifelog.push.PushTokenRepository
import com.example.lifelog.push.service.PushOrchestratorService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class PushScheduler(
    private val props: PushPolicyProperties,
    private val tokenRepo: PushTokenRepository,
    private val orchestrator: PushOrchestratorService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelayString = "10000") // 10분
    fun scanAndSend() {
        log.info("[PUSH] scheduler tick enabled={}", props.enabled)
        if (!props.enabled) return

        val userIds = tokenRepo.findDistinctEnabledUserIds()
        log.info("[PUSH] target users count={}", userIds.size)
        if (userIds.isEmpty()) return

        userIds.forEach { userId ->
            val result = runCatching { orchestrator.tickUser(userId) }
            result.onFailure { e -> log.warn("[PUSH] tickUser failed userId={}", userId, e) }
        }
    }
}
