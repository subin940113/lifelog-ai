package com.example.lifelog.application.insight.pipeline

import com.example.lifelog.domain.log.RawLogCreatedEvent
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class RawLogCreatedInsightListener(
    private val generateInsightUseCase: GenerateInsightUseCase,
) {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onRawLogCreated(e: RawLogCreatedEvent) {
        // 이벤트에 RawLog가 들어있다는 전제(너 코드 기준 RawLogCreatedEvent(saved))
        generateInsightUseCase.execute(e.rawLog)
    }
}
