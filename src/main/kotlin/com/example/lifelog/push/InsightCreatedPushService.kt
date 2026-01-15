package com.example.lifelog.push

import com.example.lifelog.push.fcm.FcmClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId
import java.time.ZonedDateTime

@Service
class InsightCreatedPushService(
    private val tokenRepo: PushTokenRepository,
    private val sendLogRepo: PushSendLogRepository,
    private val fcm: FcmClient,
    private val props: PushPolicyProperties,
) {
    private val zone: ZoneId = ZoneId.of(props.zone)

    @Transactional
    fun onInsightCreated(
        userId: Long,
        insightId: Long,
        insightTitle: String,
    ) {
        val cfg = props.insightCreated
        if (!props.enabled || !cfg.enabled) return

        val now = ZonedDateTime.now(zone)
        val today = now.toLocalDate()

        // (1) 일일 한도
        val sentCount =
            sendLogRepo.countByUserIdAndTypeAndLocalDate(
                userId,
                PushSendType.INSIGHT_CREATED,
                today,
            )
        if (sentCount >= cfg.maxPerDay) return

        // (2) 디바이스 토큰
        val tokens = tokenRepo.findByUserIdAndEnabledTrue(userId)
        if (tokens.isEmpty()) return

        // (3) 메시지
        val title = props.message.insightTitle
        val body = props.message.insightBodyTemplate.format(insightTitle)

        // (4) 발송
        tokens.forEach { t ->
            fcm.send(
                token = t.token,
                title = title,
                body = body,
                data =
                    mapOf(
                        PushIntentDataKeys.INTENT_TYPE to PushIntentTypes.INSIGHT_DETAIL,
                        PushIntentDataKeys.INSIGHT_ID to insightId.toString(),
                    ),
            )
        }

        // (5) 로그 기록
        sendLogRepo.save(
            PushSendLog(
                userId = userId,
                type = PushSendType.INSIGHT_CREATED,
                localDate = today,
                keyword = null,
            ),
        )
    }
}
