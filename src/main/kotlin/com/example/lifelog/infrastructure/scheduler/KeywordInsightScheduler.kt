package com.example.lifelog.infrastructure.scheduler

import com.example.lifelog.application.signal.GenerateKeywordInsightUseCase
import com.example.lifelog.domain.user.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class KeywordInsightScheduler(
    private val userRepository: UserRepository,
    private val generateKeywordInsightUseCase: GenerateKeywordInsightUseCase,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 */10 * * * *") // 10분마다 예시
    fun tick() {
        val userIds = userRepository.findAll().map { it.id }
        var total = 0

        for (userId in userIds) {
            try {
                val count = generateKeywordInsightUseCase.execute(userId)
                total += count
            } catch (e: Exception) {
                log.warn("[INSIGHT] generate failed userId={}", userId, e)
            }
        }

        log.info("[INSIGHT] scheduler tick done totalGenerated={}", total)
    }
}
