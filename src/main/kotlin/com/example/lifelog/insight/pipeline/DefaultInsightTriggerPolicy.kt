package com.example.lifelog.insight.pipeline

import com.example.lifelog.insight.InsightGateService
import com.example.lifelog.insight.feedback.InsightPreferenceReader
import com.example.lifelog.log.raw.RawLog
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Instant
import kotlin.math.max

@Component
class DefaultInsightTriggerPolicy(
    private val gateService: InsightGateService,
    private val props: InsightPolicyProperties,
    private val cooldownRepo: InsightCooldownRepository,
    private val preferenceReader: InsightPreferenceReader, // ✅ 5번: 사용자 피드백 기반 보수화
    private val clock: Clock = Clock.systemUTC(),
) : InsightTriggerPolicy {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun decide(
        userId: Long,
        rawLog: RawLog,
    ): InsightTriggerDecision {
        val gate = gateService.getGate(userId)

        // 🔍 Gate 상태 로깅
        if (!gate.enabled) {
            log.info(
                "[InsightTrigger] gate disabled | userId={} keywords={} rawLength={}",
                userId,
                gate.keywords.size,
                rawLog.content.length,
            )
            return InsightTriggerDecision(false, "gate_disabled")
        }

        val content = rawLog.content.trim()
        if (content.length < props.minChars) {
            log.info(
                "[InsightTrigger] too short | userId={} len={} minChars={}",
                userId,
                content.length,
                props.minChars,
            )
            return InsightTriggerDecision(false, "too_short")
        }

        // ✅ 5번: 최근 피드백 기반 보수화(싫어요 연속)
        val dislikeStreak = preferenceReader.dislikeStreak(userId)

        // 키워드 매칭
        val matched = findFirstMatch(content, gate.keywords)
        if (props.keywordMatchRequired && matched == null) {
            log.info(
                "[InsightTrigger] no keyword match | userId={} keywords={} contentSnippet={}",
                userId,
                gate.keywords,
                content.take(60),
            )
            return InsightTriggerDecision(false, "no_keyword_match")
        }

        val now = Instant.now(clock)
        val last = cooldownRepo.getLastRunAt(userId)

        // 쿨다운 (기본)
        if (last != null) {
            val elapsed = now.epochSecond - last.epochSecond
            if (elapsed < props.cooldownSeconds) {
                log.info(
                    "[InsightTrigger] cooldown | userId={} elapsed={} cooldown={} dislikeStreak={}",
                    userId,
                    elapsed,
                    props.cooldownSeconds,
                    dislikeStreak,
                )
                return InsightTriggerDecision(false, "cooldown")
            }
        }

        // ✅ 5번: soft cooldown (싫어요 연속이면 더 오래 쉬게)
        // - streak 0~2 : 추가 없음
        // - streak 3~4 : +50%
        // - streak 5~6 : +100%
        // - streak 7+  : +200%
        val extraCooldownSeconds = extraCooldownSeconds(dislikeStreak, props.cooldownSeconds)

        if (extraCooldownSeconds > 0 && last != null) {
            val elapsed = now.epochSecond - last.epochSecond
            val required = props.cooldownSeconds + extraCooldownSeconds
            if (elapsed < required) {
                log.info(
                    "[InsightTrigger] soft cooldown | userId={} elapsed={} required={} base={} extra={} dislikeStreak={}",
                    userId,
                    elapsed,
                    required,
                    props.cooldownSeconds,
                    extraCooldownSeconds,
                    dislikeStreak,
                )
                return InsightTriggerDecision(false, "soft_cooldown")
            }
        }

        // ✅ 5번: streak가 높으면 "키워드만"으로 생성하지 않도록 보수화
        // - streak >= 3 : 변화/대비/시간 신호가 없으면 스킵
        if (dislikeStreak >= props.dislikeStreakConservativeThreshold) {
            val hasChangeSignal = hasChangeOrContrastSignal(content)
            if (!hasChangeSignal) {
                log.info(
                    "[InsightTrigger] conservative skip(no change/contrast) | userId={} matchedKeyword={} dislikeStreak={} snippet={}",
                    userId,
                    matched,
                    dislikeStreak,
                    content.take(80),
                )
                return InsightTriggerDecision(false, "conservative_no_change")
            }
        }

        // 일일 제한
        if (props.dailyLimit > 0) {
            val todayCount = cooldownRepo.getTodayCount(userId, now)
            val effectiveDailyLimit = effectiveDailyLimit(props.dailyLimit, dislikeStreak)

            if (todayCount >= effectiveDailyLimit) {
                log.info(
                    "[InsightTrigger] daily limit | userId={} count={} limit={} effectiveLimit={} dislikeStreak={}",
                    userId,
                    todayCount,
                    props.dailyLimit,
                    effectiveDailyLimit,
                    dislikeStreak,
                )
                return InsightTriggerDecision(false, "daily_limit")
            }
        }

        log.info(
            "[InsightTrigger] OK | userId={} matchedKeyword={} dislikeStreak={}",
            userId,
            matched,
            dislikeStreak,
        )

        return InsightTriggerDecision(
            shouldRun = true,
            reason = "ok",
            matchedKeyword = matched,
        )
    }

    private fun findFirstMatch(
        content: String,
        keywords: List<String>,
    ): String? {
        val lc = content.lowercase()
        for (k in keywords) {
            val kk = k.trim()
            if (kk.isEmpty()) continue
            if (lc.contains(kk.lowercase())) return kk
        }
        return null
    }

    /**
     * ✅ 5번: 보수화 게이트(초저비용)
     * - "요즘/최근/갑자기/예전/달라/변화" 같은 시간/변화 marker
     * - "근데/하지만/반면" 같은 대비 marker
     * - "~잘 안", "~안 와" 같은 패턴 변화 marker
     */
    private fun hasChangeOrContrastSignal(text: String): Boolean {
        val t = text.lowercase()

        val markers =
            listOf(
                // change/time
                "요즘",
                "최근",
                "갑자기",
                "예전",
                "전에는",
                "원래",
                "달라",
                "변화",
                "이전엔",
                "lately",
                "recently",
                "suddenly",
                "used to",
                "not anymore",
                "no longer",
                "different",
                // contrast/pivot
                "근데",
                "하지만",
                "그런데",
                "반면",
                "그러나",
                "yet",
                "but",
                "however",
                "though",
                "while",
                // behavior change
                "잘 안",
                "안 하",
                "안 와",
                "안 옴",
                "못 하",
                "못 함",
            )

        return markers.any { t.contains(it) }
    }

    /**
     * dislikeStreak가 높을수록 추가 쿨다운을 부여해 '연타'를 막습니다.
     */
    private fun extraCooldownSeconds(
        dislikeStreak: Int,
        baseCooldownSeconds: Long,
    ): Long {
        if (dislikeStreak < 3) return 0L

        val multiplier =
            when (dislikeStreak) {
                in 3..4 -> 0.5
                in 5..6 -> 1.0
                else -> 2.0
            }

        // 최소 30초는 주도록(너무 짧은 base 대비)
        return max((baseCooldownSeconds * multiplier).toLong(), 30L)
    }

    /**
     * dislikeStreak가 높으면 일일 생성 상한도 살짝 낮춰 "불쾌한 경험의 반복"을 줄입니다.
     * - streak 0~2 : 그대로
     * - streak 3~5 : -1
     * - streak 6+  : -2
     */
    private fun effectiveDailyLimit(
        baseDailyLimit: Int,
        dislikeStreak: Int,
    ): Int {
        if (baseDailyLimit <= 0) return baseDailyLimit
        val penalty =
            when (dislikeStreak) {
                in 0..2 -> 0
                in 3..5 -> 1
                else -> 2
            }
        return max(1, baseDailyLimit - penalty)
    }
}
