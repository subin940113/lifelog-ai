package com.example.lifelog.application.insight.pipeline

import com.example.lifelog.application.push.InsightCreatedPushService
import com.example.lifelog.domain.insight.InsightCreatedEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class InsightCreatedPushListener(
    private val push: InsightCreatedPushService,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: InsightCreatedEvent) {
        // 실패해도 인사이트 생성은 성공했으니, 예외가 전파되지 않게 막는 게 일반적으로 안전함
        runCatching {
            push.onInsightCreated(
                userId = event.userId,
                insightId = event.insightId,
                insightTitle = event.title,
            )
        }
    }
}
