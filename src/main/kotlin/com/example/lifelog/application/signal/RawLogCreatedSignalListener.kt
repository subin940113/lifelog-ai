package com.example.lifelog.application.signal

import com.example.lifelog.domain.log.RawLogCreatedEvent
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class RawLogCreatedSignalListener(
    private val updateSignalUseCase: UpdateSignalUseCase,
) {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onRawLogCreated(e: RawLogCreatedEvent) {
        updateSignalUseCase.execute(e.rawLog)
    }
}
