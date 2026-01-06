package com.example.lifelog.insight.pipeline

import com.example.lifelog.log.event.RawLogCreatedEvent
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class RawLogCreatedInsightListener(
    private val insightGenerationService: InsightGenerationService,
) {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onRawLogCreated(e: RawLogCreatedEvent) {
        // 이벤트에 RawLog가 들어있다는 전제(너 코드 기준 RawLogCreatedEvent(saved))
        insightGenerationService.generateIfNeeded(e.rawLog)
    }
}
