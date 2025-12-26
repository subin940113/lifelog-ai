package com.example.lifelog.ai

import com.example.lifelog.log.event.RawLogCreatedEvent
import com.example.lifelog.log.structured.StructuredEvent
import com.example.lifelog.log.structured.StructuredEventRepository
import com.example.lifelog.structuring.domain.StructuringRequest
import com.example.lifelog.structuring.domain.StructuringResult
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class AiWorkerListener(
    private val structuredEventRepository: StructuredEventRepository,
    private val structuringApplicationService: com.example.lifelog.structuring.StructuringApplicationService,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * RawLog 생성 이벤트를 수신하면, structurer(현재 설정 모드)에 따라 구조화하고 StructuredEvent로 저장한다.
     */
    @Async
    @EventListener
    fun handle(event: RawLogCreatedEvent) {
        val raw = event.rawLog

        val result =
            structuringApplicationService.createStructuredEvent(
                StructuringRequest(
                    userId = raw.userId,
                    rawLogId = raw.id,
                    createdAt = raw.createdAt,
                    content = raw.content,
                ),
            )

        when (result) {
            is StructuringResult.Success -> {
                if (result.drafts.isEmpty()) {
                    log.warn(
                        "[AI-Worker] Structuring returned empty drafts. rawLogId={}, structurer={}",
                        raw.id,
                        result.meta.structurer,
                    )
                    return
                }

                result.drafts.forEach { draft ->
                    val payloadJson =
                        objectMapper.writeValueAsString(
                            mapOf(
                                "subject" to
                                    mapOf(
                                        "kind" to draft.subject.kind.name,
                                        "name" to draft.subject.name,
                                        "species" to draft.subject.species,
                                    ),
                                "type" to draft.type.name,
                                "tags" to draft.tags,
                                "payload" to draft.payload,
                                "meta" to
                                    mapOf(
                                        "structurer" to result.meta.structurer,
                                        "promptVersion" to result.meta.promptVersion,
                                        "model" to result.meta.model,
                                        "latencyMs" to result.meta.latencyMs,
                                    ),
                            ),
                        )

                    val structuredEvent =
                        StructuredEvent(
                            userId = raw.userId,
                            rawLogId = raw.id,
                            category = draft.type.name,
                            occurredAt = draft.occurredAt ?: raw.createdAt, // null이면 작성 시각으로 보정(운영 정책)
                            confidence = draft.confidence,
                            payload = payloadJson,
                        )

                    structuredEventRepository.save(structuredEvent)

                    log.info(
                        "[AI-Worker] StructuredEvent saved. rawLogId={}, category={}, structurer={}",
                        raw.id,
                        draft.type.name,
                        result.meta.structurer,
                    )
                }
            }

            is StructuringResult.Failure -> {
                // MVP에서는 실패 시 저장하지 않고 로그만 남긴다. (원하면 실패 이벤트 테이블/재시도 큐를 붙일 수 있음)
                log.warn(
                    "[AI-Worker] Structuring failed. rawLogId={}, structurer={}, error={}",
                    raw.id,
                    result.meta.structurer,
                    result.error,
                )
            }
        }
    }
}
