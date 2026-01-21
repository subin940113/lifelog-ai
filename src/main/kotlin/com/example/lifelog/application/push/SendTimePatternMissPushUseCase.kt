package com.example.lifelog.application.push

import com.example.lifelog.domain.log.LogRepository
import com.example.lifelog.domain.push.PushIntentDataKeys
import com.example.lifelog.domain.push.PushIntentTypes
import com.example.lifelog.domain.push.PushSendLog
import com.example.lifelog.domain.push.PushSendLogRepository
import com.example.lifelog.domain.push.PushSendType
import com.example.lifelog.domain.push.PushSettingRepository
import com.example.lifelog.domain.push.PushTokenRepository
import com.example.lifelog.infrastructure.analyzer.TimePatternAnalyzer
import com.example.lifelog.infrastructure.config.PushPolicyProperties
import com.example.lifelog.infrastructure.external.fcm.FcmClient
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/**
 * 시간 패턴 기반 푸시 발송 Use Case
 */
@Service
class SendTimePatternMissPushUseCase(
    private val pushSettingRepository: PushSettingRepository,
    private val pushTokenRepository: PushTokenRepository,
    private val pushSendLogRepository: PushSendLogRepository,
    private val logRepository: LogRepository,
    private val fcmClient: FcmClient,
    private val properties: PushPolicyProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val zone: ZoneId = ZoneId.of(properties.zone)

    @Transactional
    fun execute(userId: Long) {
        val config = properties.timePatternMiss
        if (!properties.enabled || !config.enabled) return

        // 유저 토글 OFF면 중단
        val pushSetting = pushSettingRepository.findById(userId)
        val userEnabled = pushSetting?.enabled ?: true
        if (!userEnabled) return

        val now = ZonedDateTime.now(zone)

        // 1) 오늘 이미 보냈으면 중단 (maxPerDay=1 기본)
        val sentCount =
            pushSendLogRepository.countByUserIdAndTypeAndLocalDate(
                userId,
                PushSendType.TIME_PATTERN_MISS,
                now.toLocalDate(),
            )
        if (sentCount >= config.maxPerDay) return

        // 2) 오늘 로그가 이미 있으면 중단
        val todayStart = now.toLocalDate().atStartOfDay(zone).toInstant()
        val tomorrowStart = todayStart.plus(1, ChronoUnit.DAYS)
        if (logRepository.existsByUserIdAndCreatedAtBetween(userId, todayStart, tomorrowStart)) return

        // 3) lookback 로그 가져오기
        val start = now.minusDays(config.lookbackDays.toLong()).toInstant()
        val end = now.toInstant()
        val logSlice = logRepository.findSliceBetween(userId, start, end, PageRequest.of(0, 400))
        if (logSlice.isEmpty()) return

        val analyzer = TimePatternAnalyzer(zone, config.bucketMinutes)
        val pattern =
            analyzer.detectMostFrequentBucket(logSlice.map { it.createdAt to it.content })
                ?: return

        if (pattern.activeDays < config.minActiveDays) return

        // 4) "패턴 버킷 시간대" + triggerDelay 이후인지 체크
        val bucketStart = pattern.bucketStartMinute
        val triggerMinute = bucketStart + config.bucketMinutes + config.triggerDelayMinutes
        val nowMinute = now.hour * 60 + now.minute
        if (nowMinute < triggerMinute) return

        // 5) 토큰 없으면 중단
        val tokens = pushTokenRepository.findByUserIdAndEnabledTrue(userId)
        if (tokens.isEmpty()) return

        // 6) 전송
        tokens.forEach { pushToken ->
            runCatching {
                fcmClient.send(
                    token = pushToken.token,
                    title = properties.message.timePatternTitle,
                    body = properties.message.timePatternBody,
                    data =
                        mapOf(
                            PushIntentDataKeys.INTENT_TYPE to PushIntentTypes.RECORD_PROMPT,
                        ),
                )
            }.onFailure { exception ->
                log.warn(
                    "[PUSH] TIME_PATTERN_MISS send failed userId={} tokenPrefix={}",
                    userId,
                    pushToken.token.take(8),
                    exception,
                )
            }
        }

        // 7) send log 기록
        pushSendLogRepository.save(
            PushSendLog(
                userId = userId,
                type = PushSendType.TIME_PATTERN_MISS,
                localDate = now.toLocalDate(),
            ),
        )
    }
}
