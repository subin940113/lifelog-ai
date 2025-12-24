package com.example.lifelog.log.structured

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/events")
class StructuredEventController(
    private val structuredEventRepository: StructuredEventRepository,
) {
    data class StructuredEventResponse(
        val id: Long,
        val category: String,
        val occurredAt: String?,
        val confidence: Double,
        val payload: String,
        val rawLogId: Long,
    )

    @GetMapping
    fun list(
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) rawLogId: Long?,
    ): List<StructuredEventResponse> {
        val userId = 1L // MVP: 임시

        val events =
            when {
                rawLogId != null ->
                    structuredEventRepository.findAllByUserIdAndRawLogIdOrderByCreatedAtDesc(
                        userId,
                        rawLogId,
                    )

                !category.isNullOrBlank() ->
                    structuredEventRepository.findAllByUserIdAndCategoryOrderByOccurredAtDesc(
                        userId,
                        category,
                    )

                else -> structuredEventRepository.findAllByUserIdOrderByOccurredAtDesc(userId)
            }

        return events.map {
            StructuredEventResponse(
                id = it.id,
                category = it.category,
                occurredAt = it.occurredAt?.toString(),
                confidence = it.confidence,
                payload = it.payload,
                rawLogId = it.rawLogId,
            )
        }
    }
}
