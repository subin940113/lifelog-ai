package com.example.lifelog.structuring.impl

import com.example.lifelog.infra.openai.OpenAiClient
import com.example.lifelog.structuring.domain.EventType
import com.example.lifelog.structuring.domain.StructuredEventDraft
import com.example.lifelog.structuring.domain.StructuringError
import com.example.lifelog.structuring.domain.StructuringMeta
import com.example.lifelog.structuring.domain.StructuringRequest
import com.example.lifelog.structuring.domain.StructuringResult
import com.example.lifelog.structuring.domain.SubjectKind
import com.example.lifelog.structuring.domain.SubjectRef
import com.example.lifelog.structuring.port.EventStructurer
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

@Component
class LlmEventStructurer(
    private val openAiClient: OpenAiClient,
    private val objectMapper: ObjectMapper,
) : EventStructurer {
    override fun structure(request: StructuringRequest): StructuringResult =
        try {
            // 1) prompt 파일 로딩
            val systemPrompt = loadPrompt("/prompts/prompt_system.txt")
            val userTemplate = loadPrompt("/prompts/prompt_user.txt")
            val userPrompt = userTemplate.replace("{{LOG_CONTENT}}", request.content)

            // 2) schema 파일 로딩
            val schemaJson = openAiClient.loadJsonSchema("/schemas/event-schema.json")

            // 3) OpenAI 호출 (schema 기반)
            val jsonOutput =
                openAiClient
                    .structureWithSchema(
                        system = systemPrompt,
                        user = userPrompt,
                        schemaJson = schemaJson,
                    ).trim()

            // 4) JSON 문자열 → List<JsonNode> or List<Map>
            val rootNode = objectMapper.readTree(jsonOutput)
            val nodes = if (rootNode.isArray) rootNode.toList() else listOf(rootNode)

            val drafts =
                nodes.mapNotNull { node ->
                    // subject
                    val subjectNode = node["subject"]
                    val subject =
                        if (subjectNode != null && !subjectNode.isNull) {
                            val kindText = subjectNode["kind"]?.asText()?.uppercase()
                            val kind = if (kindText == "PET") SubjectKind.PET else SubjectKind.HUMAN
                            val name = subjectNode["name"]?.asText()
                            val species = subjectNode["species"]?.asText()
                            SubjectRef(kind = kind, name = name, species = species)
                        } else {
                            SubjectRef(kind = SubjectKind.HUMAN, name = null, species = null)
                        }

                    // type
                    val typeText = node["type"]?.asText()?.uppercase()
                    val type = EventType.values().find { it.name == typeText } ?: EventType.OTHER

                    // tags
                    val tags = node["tags"]?.mapNotNull { it.asText() } ?: emptyList()

                    val occurredAt =
                        node["occurredAt"]
                            ?.takeIf { !it.isNull }
                            ?.asText()
                            ?.let { parseOccurredAt(it) }

                    val confidence = node["confidence"]?.asDouble() ?: 0.0

                    val payloadNode = node["payload"]
                    val payload: Map<String, Any?> =
                        if (payloadNode == null || payloadNode.isNull) {
                            emptyMap()
                        } else {
                            objectMapper.convertValue(
                                payloadNode,
                                object : TypeReference<Map<String, Any?>>() {},
                            )
                        }

                    StructuredEventDraft(
                        subject = subject,
                        type = type,
                        tags = tags,
                        occurredAt = occurredAt,
                        confidence = confidence,
                        payload = normalizePayload(payload, request.content),
                    )
                }

            StructuringResult.Success(
                drafts = drafts,
                meta =
                    StructuringMeta(
                        structurer = "openai",
                        promptVersion = "structured-schema-v1",
                    ),
            )
        } catch (e: Exception) {
            StructuringResult.Failure(
                error =
                    StructuringError.ExternalFailure(
                        message = e.message ?: "OpenAI 호출 실패",
                        cause = e,
                    ),
                meta =
                    StructuringMeta(
                        structurer = "openai",
                        promptVersion = "structured-schema-v1",
                    ),
            )
        }

    private fun loadPrompt(resourcePath: String): String =
        javaClass.getResource(resourcePath)?.readText()
            ?: throw IllegalArgumentException("Prompt not found: $resourcePath")

    private fun parseOccurredAt(raw: String): Instant? {
        val value = raw.trim()
        if (value.isBlank() || value.equals("null", true)) return null

        return try {
            if (value.endsWith("Z") || value.contains("+")) {
                runCatching { Instant.parse(value) }
                    .getOrElse { OffsetDateTime.parse(value).toInstant() }
            } else {
                LocalDateTime
                    .parse(value)
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .toInstant()
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun normalizePayload(
        payload: Map<String, Any?>,
        originalContent: String,
    ): Map<String, Any?> {
        val result = payload.toMutableMap()
        if (!result.containsKey("originalContent")) {
            result["originalContent"] = originalContent
        }
        return result
    }
}
