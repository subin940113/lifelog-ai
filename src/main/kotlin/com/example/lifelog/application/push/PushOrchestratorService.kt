package com.example.lifelog.application.push

import com.example.lifelog.domain.push.PushSendLogRepository
import com.example.lifelog.domain.push.PushSendType
import com.example.lifelog.infrastructure.config.PushPolicyProperties
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * 푸시 발송 오케스트레이터 서비스
 */
@Service
class PushOrchestratorService(
    private val timePatternMissPushService: TimePatternMissPushService,
    private val keywordNudgePushService: KeywordNudgePushService,
    private val pushSendLogRepository: PushSendLogRepository,
    private val properties: PushPolicyProperties,
) {
    private val zone: ZoneId = ZoneId.of(properties.zone)

    @Transactional
    fun tickUser(userId: Long) {
        if (!properties.enabled) return

        val today = ZonedDateTime.now(zone).toLocalDate()

        // 1) TimePatternMiss
        if (properties.timePatternMiss.enabled) {
            timePatternMissPushService.tick(userId)
        }

        // 2) 오늘 TimePatternMiss가 이미 발송되었다면 Keyword는 생략(하루 1푸시 전략)
        val alreadyTimePattern =
            pushSendLogRepository.existsByUserIdAndTypeAndLocalDate(
                userId,
                PushSendType.TIME_PATTERN_MISS,
                today,
            )
        if (alreadyTimePattern) return

        // 3) KeywordNudge
        if (properties.keywordNudge.enabled) {
            keywordNudgePushService.tick(userId)
        }
    }
}
