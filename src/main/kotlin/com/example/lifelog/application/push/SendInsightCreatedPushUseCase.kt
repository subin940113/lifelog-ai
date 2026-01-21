package com.example.lifelog.application.push

import com.example.lifelog.domain.push.PushIntentDataKeys
import com.example.lifelog.domain.push.PushIntentTypes
import com.example.lifelog.domain.push.PushSendLog
import com.example.lifelog.domain.push.PushSendLogRepository
import com.example.lifelog.domain.push.PushSendType
import com.example.lifelog.domain.push.PushTokenRepository
import com.example.lifelog.infrastructure.config.PushPolicyProperties
import com.example.lifelog.infrastructure.external.fcm.FcmClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * 인사이트 생성 시 푸시 발송 Use Case
 */
@Service
class SendInsightCreatedPushUseCase(
    private val pushTokenRepository: PushTokenRepository,
    private val pushSendLogRepository: PushSendLogRepository,
    private val fcmClient: FcmClient,
    private val properties: PushPolicyProperties,
) {
    private val zone: ZoneId = ZoneId.of(properties.zone)

    @Transactional
    fun execute(
        userId: Long,
        insightId: Long,
        insightTitle: String,
    ) {
        val config = properties.insightCreated
        if (!properties.enabled || !config.enabled) return

        val now = ZonedDateTime.now(zone)
        val today = now.toLocalDate()

        // (1) 일일 한도
        val sentCount =
            pushSendLogRepository.countByUserIdAndTypeAndLocalDate(
                userId,
                PushSendType.INSIGHT_CREATED,
                today,
            )
        if (sentCount >= config.maxPerDay) return

        // (2) 디바이스 토큰
        val tokens = pushTokenRepository.findByUserIdAndEnabledTrue(userId)
        if (tokens.isEmpty()) return

        // (3) 메시지
        val messageTitle = properties.message.insightTitle
        val messageBody = properties.message.insightBodyTemplate.format(insightTitle)

        // (4) 발송
        tokens.forEach { pushToken ->
            fcmClient.send(
                token = pushToken.token,
                title = messageTitle,
                body = messageBody,
                data =
                    mapOf(
                        PushIntentDataKeys.INTENT_TYPE to PushIntentTypes.INSIGHT_DETAIL,
                        PushIntentDataKeys.INSIGHT_ID to insightId.toString(),
                    ),
            )
        }

        // (5) 로그 기록
        pushSendLogRepository.save(
            PushSendLog(
                userId = userId,
                type = PushSendType.INSIGHT_CREATED,
                localDate = today,
                keyword = null,
            ),
        )
    }
}
