package com.example.lifelog.application.insight.pipeline

import com.example.lifelog.domain.insight.Insight
import com.example.lifelog.domain.insight.InsightCooldownRepository
import com.example.lifelog.domain.insight.InsightCreatedEvent
import com.example.lifelog.domain.insight.InsightRepository
import com.example.lifelog.domain.log.RawLog
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

@Service
class InsightGenerationService(
    private val triggerPolicy: InsightTriggerPolicy,
    private val contextBuilder: InsightContextBuilder,
    private val generator: InsightGeneratorRouter,
    private val insightRepository: InsightRepository,
    private val cooldownRepo: InsightCooldownRepository,
    private val eventPublisher: InsightEventPublisher,
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

        val saved =
            insightRepository.save(
                Insight(
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

        // 저장 성공 시점(트랜잭션 커밋 후 푸시 발송되도록 이벤트만 발행)
        val insightId = saved.id ?: return
        eventPublisher.publishInsightCreated(
            InsightCreatedEvent(
                userId = userId,
                insightId = insightId,
                title = saved.title,
            ),
        )
    }
}
