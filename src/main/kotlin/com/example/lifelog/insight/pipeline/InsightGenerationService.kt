package com.example.lifelog.insight.pipeline

import com.example.lifelog.insight.AiInsight
import com.example.lifelog.insight.AiInsightRepository
import com.example.lifelog.log.raw.RawLog
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

@Service
class InsightGenerationService(
    private val triggerPolicy: InsightTriggerPolicy,
    private val contextBuilder: InsightContextBuilder,
    private val generator: InsightGeneratorRouter, // ✅ 단일 라우터로 주입
    private val insightRepo: AiInsightRepository,
    private val cooldownRepo: InsightCooldownRepository,
    private val clock: Clock = Clock.systemUTC(),
) {
    @Transactional
    fun generateIfNeeded(rawLog: RawLog) {
        val userId = rawLog.userId
        val content = rawLog.content.trim()
        if (content.isEmpty()) return

        val decision = triggerPolicy.decide(userId, rawLog)
        if (!decision.shouldRun) return

        val ctx = contextBuilder.build(userId, rawLog, decision.matchedKeyword)

        val gen = generator.generate(ctx) ?: return

        insightRepo.save(
            AiInsight(
                userId = userId,
                sourceLogId = ctx.sourceLogId,
                kind = gen.kind,
                title = gen.title.take(120),
                body = gen.body,
                evidence = gen.evidence,
                keyword = gen.keyword,
            ),
        )

        cooldownRepo.markRun(userId, Instant.now(clock))
    }
}
