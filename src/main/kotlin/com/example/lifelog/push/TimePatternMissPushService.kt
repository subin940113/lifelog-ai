package com.example.lifelog.push

import com.example.lifelog.log.raw.RawLogRepository
import com.example.lifelog.push.fcm.FcmClient
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Service
class TimePatternMissPushService(
    private val pushSettingRepo: PushSettingRepository,
    private val tokenRepo: PushTokenRepository,
    private val sendLogRepo: PushSendLogRepository,
    private val rawLogRepo: RawLogRepository,
    private val fcm: FcmClient,
    private val props: PushPolicyProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val zone: ZoneId = ZoneId.of(props.zone)

    @Transactional
    fun tick(userId: Long) {
        val cfg = props.timePatternMiss
        if (!props.enabled || !cfg.enabled) return

        // 유저 토글 OFF면 중단
        val userEnabled = pushSettingRepo.findById(userId).map { it.enabled }.orElse(true)
        if (!userEnabled) return

        val now = ZonedDateTime.now(zone)

        // 1) 오늘 이미 보냈으면 중단 (maxPerDay=1 기본)
        val sentCount =
            sendLogRepo.countByUserIdAndTypeAndLocalDate(
                userId,
                PushSendType.TIME_PATTERN_MISS,
                now.toLocalDate(),
            )
        if (sentCount >= cfg.maxPerDay) return

        // 2) 오늘 로그가 이미 있으면 중단
        val todayStart = now.toLocalDate().atStartOfDay(zone).toInstant()
        val tomorrowStart = todayStart.plus(1, ChronoUnit.DAYS)
        if (rawLogRepo.existsByUserIdAndCreatedAtBetween(userId, todayStart, tomorrowStart)) return

        // 3) lookback 로그 가져오기
        val start = now.minusDays(cfg.lookbackDays.toLong()).toInstant()
        val end = now.toInstant()
        val slice = rawLogRepo.findSliceBetween(userId, start, end, PageRequest.of(0, 400))
        if (slice.isEmpty()) return

        val analyzer = TimePatternAnalyzer(zone, cfg.bucketMinutes)
        val pattern =
            analyzer.detectMostFrequentBucket(slice.map { it.createdAt to it.content })
                ?: return

        if (pattern.activeDays < cfg.minActiveDays) return

        // 4) “패턴 버킷 시간대” + triggerDelay 이후인지 체크
        val bucketStart = pattern.bucketStartMinute
        val triggerMinute = bucketStart + cfg.bucketMinutes + cfg.triggerDelayMinutes
        val nowMinute = now.hour * 60 + now.minute
        if (nowMinute < triggerMinute) return

        // 5) 토큰 없으면 중단 (✅ 변경)
        val tokens = tokenRepo.findByUserIdAndEnabledTrue(userId)
        if (tokens.isEmpty()) return

        // 6) 전송
        tokens.forEach { t ->
            runCatching {
                fcm.send(
                    token = t.token,
                    title = props.message.timePatternTitle,
                    body = props.message.timePatternBody,
                    data =
                        mapOf(
                            PushIntentDataKeys.INTENT_TYPE to PushIntentTypes.RECORD_PROMPT,
                        ),
                )
            }.onFailure { e ->
                log.warn(
                    "[PUSH] TIME_PATTERN_MISS send failed userId={} tokenPrefix={}",
                    userId,
                    t.token.take(8),
                    e,
                )
                // (선택) 실패 토큰 자동 비활성화 전략을 넣으려면 여기에서 처리
                // t.enabled = false
                // tokenRepo.save(t)
            }
        }

        // 7) send log 기록
        sendLogRepo.save(
            PushSendLog(
                userId = userId,
                type = PushSendType.TIME_PATTERN_MISS,
                localDate = now.toLocalDate(),
            ),
        )
    }
}
