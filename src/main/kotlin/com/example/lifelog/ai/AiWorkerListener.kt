package com.example.lifelog.ai

import com.example.lifelog.log.event.RawLogCreatedEvent
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class AiWorkerListener {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * MVP 단계: 실제 AI 호출/구조화 저장 전, 이벤트 수신 여부만 확인한다.
     */
    @Async
    @EventListener
    fun handle(event: RawLogCreatedEvent) {
        log.info(
            "[AI-Worker][MVP] RawLog received. id={}, content={}",
            event.rawLog.id,
            event.rawLog.content
        )
        // TODO: 다음 단계에서 LLM 호출 + StructuredEvent 저장 로직 추가
    }
}