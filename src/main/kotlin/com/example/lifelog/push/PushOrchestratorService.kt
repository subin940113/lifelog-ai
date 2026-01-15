package com.example.lifelog.push.service

import com.example.lifelog.push.KeywordNudgePushService
import com.example.lifelog.push.PushPolicyProperties
import com.example.lifelog.push.PushSendLogRepository
import com.example.lifelog.push.PushSendType
import com.example.lifelog.push.TimePatternMissPushService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId
import java.time.ZonedDateTime

@Service
class PushOrchestratorService(
    private val timePattern: TimePatternMissPushService,
    private val keyword: KeywordNudgePushService,
    private val sendLogRepo: PushSendLogRepository,
    private val props: PushPolicyProperties,
) {
    private val zone: ZoneId = ZoneId.of(props.zone)

    @Transactional
    fun tickUser(userId: Long) {
        if (!props.enabled) return

        val today = ZonedDateTime.now(zone).toLocalDate()

        // 1) TimePatternMiss
        if (props.timePatternMiss.enabled) {
            timePattern.tick(userId)
        }

        // 2) 오늘 TimePatternMiss가 이미 발송되었다면 Keyword는 생략(하루 1푸시 전략)
        val alreadyTimePattern =
            sendLogRepo.existsByUserIdAndTypeAndLocalDate(
                userId,
                PushSendType.TIME_PATTERN_MISS,
                today,
            )
        if (alreadyTimePattern) return

        // 3) KeywordNudge
        if (props.keywordNudge.enabled) {
            keyword.tick(userId)
        }
    }
}
