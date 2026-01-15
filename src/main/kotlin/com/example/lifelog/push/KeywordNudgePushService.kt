package com.example.lifelog.push

import com.example.lifelog.interest.InterestKeywordRepository
import com.example.lifelog.log.raw.RawLogRepository
import com.example.lifelog.push.fcm.FcmClient
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Service
class KeywordNudgePushService(
    private val interestRepo: InterestKeywordRepository,
    private val tokenRepo: PushTokenRepository, // ✅ PushToken 테이블 기준
    private val sendLogRepo: PushSendLogRepository,
    private val rawLogRepo: RawLogRepository,
    private val fcm: FcmClient,
    private val props: PushPolicyProperties,
) {
    private val zone: ZoneId = ZoneId.of(props.zone)

    @Transactional
    fun tick(userId: Long) {
        val cfg = props.keywordNudge
        if (!props.enabled || !cfg.enabled) return

        val now = ZonedDateTime.now(zone)

        // (1) 오늘 타입별 발송 한도
        val sentCount =
            sendLogRepo.countByUserIdAndTypeAndLocalDate(
                userId,
                PushSendType.KEYWORD_NUDGE,
                now.toLocalDate(),
            )
        if (sentCount >= cfg.maxPerDay) return

        // (2) 관심 키워드 설정 확인
        // - trim/blank 제거
        // - 대소문자 무시 중복 제거(최근 등록 우선)
        val interests =
            interestRepo
                .findAllByUserIdOrderByCreatedAtDesc(userId)
                .asSequence()
                .map { it.keyword.trim() }
                .filter { it.isNotBlank() }
                .distinctBy { it.lowercase() }
                .toList()
        if (interests.isEmpty()) return

        // (3) 쿨다운 내에 보내지 않은 키워드 선택
        val cutoffDate = now.toLocalDate().minusDays(cfg.cooldownDays.toLong())

        val candidate =
            interests.firstOrNull { kw ->
                !sendLogRepo.existsByUserIdAndTypeAndKeywordAndLocalDateGreaterThanEqual(
                    userId = userId,
                    type = PushSendType.KEYWORD_NUDGE,
                    keyword = kw,
                    localDate = cutoffDate,
                )
            } ?: return

        // ✅ (4) "패턴 기반 발송 타이밍" 체크 (패턴 없으면 발송 안 함)
        // 4-1) 최근 lookbackDays 로그 슬라이스
        val start = now.minusDays(cfg.lookbackDays.toLong()).toInstant()
        val end = now.toInstant()
        val slice = rawLogRepo.findSliceBetween(userId, start, end, PageRequest.of(0, 400))
        if (slice.isEmpty()) return

        val analyzer = KeywordTimePatternAnalyzer(zone, cfg.bucketMinutes)

        val pattern =
            analyzer.detectMostFrequentBucket(
                rows = slice.map { it.createdAt to it.content },
                keyword = candidate,
            ) ?: return // ✅ 패턴 없으면 발송하지 않음

        if (pattern.occurrences < cfg.minOccurrences) return

        // 4-2) "패턴 시간대 + triggerDelay" 이후에만 발송
        // - 자정 직후(00:xx)에 바로 쏘는 문제를 막기 위해, 트리거가 다음날로 넘어가면 오늘은 발송하지 않음
        val bucketStart = pattern.bucketStartMinute
        val triggerMinute = bucketStart + cfg.bucketMinutes + cfg.triggerDelayMinutes
        if (triggerMinute >= 24 * 60) return

        val nowMinute = now.hour * 60 + now.minute
        if (nowMinute < triggerMinute) return

        // (5) 오늘 이미 "candidate 키워드 포함 로그"가 있으면 굳이 푸시 안 보냄 (선택이지만 권장)
        val todayStart = now.toLocalDate().atStartOfDay(zone).toInstant()
        val tomorrowStart = todayStart.plus(1, ChronoUnit.DAYS)
        val todaySlice = rawLogRepo.findSliceBetween(userId, todayStart, tomorrowStart, PageRequest.of(0, 200))
        if (todaySlice.any { it.content.contains(candidate, ignoreCase = true) }) return

        // (6) 디바이스(토큰)
        val tokens = tokenRepo.findByUserIdAndEnabledTrue(userId)
        if (tokens.isEmpty()) return

        val body = props.message.keywordBodyTemplate.format(candidate)

        // (7) 발송
        tokens.forEach { t ->
            fcm.send(
                token = t.token,
                title = props.message.keywordTitle,
                body = body,
                data =
                    mapOf(
                        PushIntentDataKeys.INTENT_TYPE to PushIntentTypes.RECORD_PROMPT,
                        PushIntentDataKeys.KEYWORD to candidate,
                    ),
            )
        }

        // (8) 로그 기록
        if (!sendLogRepo.existsByUserIdAndTypeAndLocalDateAndKeyword(
                userId,
                PushSendType.KEYWORD_NUDGE,
                now.toLocalDate(),
                candidate,
            )
        ) {
            sendLogRepo.save(
                PushSendLog(
                    userId = userId,
                    type = PushSendType.KEYWORD_NUDGE,
                    localDate = now.toLocalDate(),
                    keyword = candidate,
                ),
            )
        }
    }
}
