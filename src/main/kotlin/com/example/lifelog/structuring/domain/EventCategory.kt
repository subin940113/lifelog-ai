package com.example.lifelog.structuring.domain

import java.time.Instant

enum class EventCategory { SLEEP, MEAL, MOOD, OTHER }

data class StructuredEventDraft(
    val category: EventCategory,
    val occurredAt: Instant?,
    val confidence: Double,
    val payload: Map<String, Any?>,
)
