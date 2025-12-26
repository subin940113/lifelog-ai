package com.example.lifelog.structuring.impl

import com.example.lifelog.structuring.domain.EventType
import com.example.lifelog.structuring.domain.StructuredEventDraft
import com.example.lifelog.structuring.domain.StructuringError
import com.example.lifelog.structuring.domain.StructuringMeta
import com.example.lifelog.structuring.domain.StructuringRequest
import com.example.lifelog.structuring.domain.StructuringResult
import com.example.lifelog.structuring.domain.SubjectKind
import com.example.lifelog.structuring.domain.SubjectRef
import com.example.lifelog.structuring.port.EventStructurer
import org.springframework.stereotype.Component

@Component
class PseudoEventStructurer : EventStructurer {
    override fun structure(request: StructuringRequest): StructuringResult =
        try {
            // 간단한 파싱 로직: raw content를 그대로 요약/기본값으로 설정
            val content = request.content

            // 기본 subject: 인간 작성자
            val subject = SubjectRef(kind = SubjectKind.HUMAN, name = null, species = null)

            // 기본 type: OTHER(정밀한 분류가 없는 경우)
            val type = EventType.OTHER

            // tags: 원문 텍스트 일부를 간단히 태그로 분리 (예: 공백 기준 n개)
            val tags: List<String> =
                content
                    .split("\\s+".toRegex())
                    .filter { it.length >= 2 }
                    .take(3) // 너무 많아지지 않도록 최대 3개만

            val draft =
                StructuredEventDraft(
                    subject = subject,
                    type = type,
                    tags = tags,
                    occurredAt = request.createdAt,
                    confidence = 0.1,
                    payload =
                        mapOf(
                            "originalContent" to content,
                            "notes" to null,
                        ),
                )

            StructuringResult.Success(
                drafts = listOf(draft),
                meta = StructuringMeta(structurer = "pseudo", promptVersion = "pseudo-v1"),
            )
        } catch (e: Exception) {
            StructuringResult.Failure(
                error =
                    StructuringError.ExternalFailure(
                        message = "Pseudo structuring failed: ${e.message}",
                        cause = e,
                    ),
                meta = StructuringMeta(structurer = "pseudo", promptVersion = "pseudo-v1"),
            )
        }
}
