package com.example.lifelog.ai

import com.example.lifelog.log.event.RawLogCreatedEvent
import com.example.lifelog.log.structured.StructuredEvent
import com.example.lifelog.log.structured.StructuredEventRepository
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class AiWorkerListener(
    private val structuredEventRepository: StructuredEventRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * MVP 단계: 실제 AI 호출/구조화 저장 전, 이벤트 수신 여부만 확인한다.
     */
    @Async
    @EventListener
    fun handle(event: RawLogCreatedEvent) {
        val raw = event.rawLog

        // MVP: 가짜 구조화 결과
        val structuredEvent =
            StructuredEvent(
                userId = raw.userId,
                rawLogId = raw.id,
                category = "SLEEP",
                occurredAt = raw.createdAt,
                confidence = 0.8,
                payload =
                    """
                    {
                      "note": "stub structured result",
                      "originalContent": "${raw.content}"
                    }
                    """.trimIndent(),
            )

        structuredEventRepository.save(structuredEvent)

        log.info(
            "[AI-Worker][MVP] StructuredEvent saved. rawLogId={}",
            raw.id,
        )
    }
}
