package com.example.lifelog.structuring.impl

import com.example.lifelog.structuring.domain.EventCategory
import com.example.lifelog.structuring.domain.StructuredEventDraft
import com.example.lifelog.structuring.domain.StructuringError
import com.example.lifelog.structuring.domain.StructuringMeta
import com.example.lifelog.structuring.domain.StructuringRequest
import com.example.lifelog.structuring.domain.StructuringResult
import com.example.lifelog.structuring.port.EventStructurer
import org.springframework.stereotype.Component
import kotlin.system.measureTimeMillis

@Component
class PseudoEventStructurer : EventStructurer {
    override fun structure(request: StructuringRequest): StructuringResult {
        var result: StructuringResult? = null
        val latency =
            measureTimeMillis {
                val content = request.content.lowercase()

                val draft =
                    when {
                        content.contains("잠") || content.contains("수면") ->
                            StructuredEventDraft(
                                category = EventCategory.SLEEP,
                                occurredAt = null,
                                confidence = 0.5,
                                payload = mapOf("note" to "pseudo-structured"),
                            )
                        content.contains("먹") || content.contains("식사") ->
                            StructuredEventDraft(
                                category = EventCategory.MEAL,
                                occurredAt = null,
                                confidence = 0.5,
                                payload = mapOf("note" to "pseudo-structured"),
                            )
                        content.contains("기분") ->
                            StructuredEventDraft(
                                category = EventCategory.MOOD,
                                occurredAt = null,
                                confidence = 0.5,
                                payload = mapOf("note" to "pseudo-structured"),
                            )
                        else ->
                            StructuredEventDraft(
                                category = EventCategory.OTHER,
                                occurredAt = null,
                                confidence = 0.3,
                                payload = mapOf("note" to "pseudo-structured"),
                            )
                    }

                result =
                    StructuringResult.Success(
                        draft = draft,
                        meta = StructuringMeta(structurer = "pseudo", latencyMs = null),
                    )
            }

        return when (val r = result) {
            is StructuringResult.Success -> r.copy(meta = r.meta.copy(latencyMs = latency))
            is StructuringResult.Failure -> r.copy(meta = r.meta.copy(latencyMs = latency))
            null -> StructuringResult.Failure(StructuringError.Unknown("no result"))
        }
    }
}
