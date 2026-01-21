package com.example.lifelog.application.push

import com.example.lifelog.domain.interest.InterestRepository
import com.example.lifelog.domain.log.LogRepository
import com.example.lifelog.domain.push.PushIntentDataKeys
import com.example.lifelog.domain.push.PushIntentTypes
import com.example.lifelog.domain.push.PushSendLog
import com.example.lifelog.domain.push.PushSendLogRepository
import com.example.lifelog.domain.push.PushSendType
import com.example.lifelog.domain.push.PushTokenRepository
import com.example.lifelog.infrastructure.analyzer.KeywordTimePatternAnalyzer
import com.example.lifelog.infrastructure.config.PushPolicyProperties
import com.example.lifelog.infrastructure.external.fcm.FcmClient
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/**
 * 키워드 기반 푸시 발송 Use Case
 */
@Service
class SendKeywordNudgePushUseCase(
    private val interestRepository: InterestRepository,
    private val pushTokenRepository: PushTokenRepository,
    private val pushSendLogRepository: PushSendLogRepository,
    private val logRepository: LogRepository,
    private val fcmClient: FcmClient,
    private val properties: PushPolicyProperties,
) {
    private val zone: ZoneId = ZoneId.of(properties.zone)

    @Transactional
    fun execute(userId: Long) {
        val config = properties.keywordNudge
        if (!properties.enabled || !config.enabled) return

        val now = ZonedDateTime.now(zone)

        // (1) 오늘 타입별 발송 한도
        val sentCount =
            pushSendLogRepository.countByUserIdAndTypeAndLocalDate(
                userId,
                PushSendType.KEYWORD_NUDGE,
                now.toLocalDate(),
            )
        if (sentCount >= config.maxPerDay) return

        // (2) 관심 키워드 설정 확인
        val interestKeywords =
            interestRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId)
                .asSequence()
                .map { it.keyword.trim() }
                .filter { it.isNotBlank() }
                .distinctBy { it.lowercase() }
                .toList()
        if (interestKeywords.isEmpty()) return

        // (3) 쿨다운 내에 보내지 않은 키워드 선택
        val cutoffDate = now.toLocalDate().minusDays(config.cooldownDays.toLong())

        val candidateKeyword =
            interestKeywords.firstOrNull { keyword ->
                !pushSendLogRepository.existsByUserIdAndTypeAndKeywordAndLocalDateGreaterThanEqual(
                    userId = userId,
                    type = PushSendType.KEYWORD_NUDGE,
                    keyword = keyword,
                    localDate = cutoffDate,
                )
            } ?: return

        // (4) "패턴 기반 발송 타이밍" 체크
        val start = now.minusDays(config.lookbackDays.toLong()).toInstant()
        val end = now.toInstant()
        val logSlice = logRepository.findSliceBetween(userId, start, end, PageRequest.of(0, 400))
        if (logSlice.isEmpty()) return

        val analyzer = KeywordTimePatternAnalyzer(zone, config.bucketMinutes)

        val pattern =
            analyzer.detectMostFrequentBucket(
                rows = logSlice.map { it.createdAt to it.content },
                keyword = candidateKeyword,
            ) ?: return

        if (pattern.occurrences < config.minOccurrences) return

        // 4-2) "패턴 시간대 + triggerDelay" 이후에만 발송
        val bucketStart = pattern.bucketStartMinute
        val triggerMinute = bucketStart + config.bucketMinutes + config.triggerDelayMinutes
        if (triggerMinute >= 24 * 60) return

        val nowMinute = now.hour * 60 + now.minute
        if (nowMinute < triggerMinute) return

        // (5) 오늘 이미 "candidate 키워드 포함 로그"가 있으면 굳이 푸시 안 보냄
        val todayStart = now.toLocalDate().atStartOfDay(zone).toInstant()
        val tomorrowStart = todayStart.plus(1, ChronoUnit.DAYS)
        val todaySlice = logRepository.findSliceBetween(userId, todayStart, tomorrowStart, PageRequest.of(0, 200))
        if (todaySlice.any { it.content.contains(candidateKeyword, ignoreCase = true) }) return

        // (6) 디바이스(토큰)
        val tokens = pushTokenRepository.findByUserIdAndEnabledTrue(userId)
        if (tokens.isEmpty()) return

        val messageBody = properties.message.keywordBodyTemplate.format(candidateKeyword)

        // (7) 발송
        tokens.forEach { pushToken ->
            fcmClient.send(
                token = pushToken.token,
                title = properties.message.keywordTitle,
                body = messageBody,
                data =
                    mapOf(
                        PushIntentDataKeys.INTENT_TYPE to PushIntentTypes.RECORD_PROMPT,
                        PushIntentDataKeys.KEYWORD to candidateKeyword,
                    ),
            )
        }

        // (8) 로그 기록
        if (!pushSendLogRepository.existsByUserIdAndTypeAndLocalDateAndKeyword(
                userId,
                PushSendType.KEYWORD_NUDGE,
                now.toLocalDate(),
                candidateKeyword,
            )
        ) {
            pushSendLogRepository.save(
                PushSendLog(
                    userId = userId,
                    type = PushSendType.KEYWORD_NUDGE,
                    localDate = now.toLocalDate(),
                    keyword = candidateKeyword,
                ),
            )
        }
    }
}
