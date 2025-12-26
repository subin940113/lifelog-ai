package com.example.lifelog.structuring.domain

import java.time.Instant

enum class SubjectKind { HUMAN, PET }

data class SubjectRef(
    val kind: SubjectKind,
    val name: String? = null,
    val species: String? = null, // "DOG" | "CAT" | "OTHER" | null
)

enum class EventType { ACTION, STATE, SYMPTOM, OBSERVATION, OTHER }

data class StructuredEventDraft(
    val subject: SubjectRef,
    val type: EventType,
    val tags: List<String>,
    val occurredAt: Instant?,
    val confidence: Double,
    val payload: Map<String, Any?>,
)
