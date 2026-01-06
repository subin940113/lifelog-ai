package com.example.lifelog.insight.pipeline

import com.example.lifelog.insight.InsightGateService
import com.example.lifelog.log.raw.RawLog
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Instant

@Component
class DefaultInsightTriggerPolicy(
    private val gateService: InsightGateService,
    private val props: InsightPolicyProperties,
    private val cooldownRepo: InsightCooldownRepository,
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

        // 쿨다운
        val now = Instant.now(clock)
        val last = cooldownRepo.getLastRunAt(userId)
        if (last != null) {
            val elapsed = now.epochSecond - last.epochSecond
            if (elapsed < props.cooldownSeconds) {
                log.info(
                    "[InsightTrigger] cooldown | userId={} elapsed={} cooldown={}",
                    userId,
                    elapsed,
                    props.cooldownSeconds,
                )
                return InsightTriggerDecision(false, "cooldown")
            }
        }

        // 일일 제한
        if (props.dailyLimit > 0) {
            val todayCount = cooldownRepo.getTodayCount(userId, now)
            if (todayCount >= props.dailyLimit) {
                log.info(
                    "[InsightTrigger] daily limit | userId={} count={} limit={}",
                    userId,
                    todayCount,
                    props.dailyLimit,
                )
                return InsightTriggerDecision(false, "daily_limit")
            }
        }

        log.info(
            "[InsightTrigger] OK | userId={} matchedKeyword={}",
            userId,
            matched,
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
}
