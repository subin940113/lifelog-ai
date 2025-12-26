package com.example.lifelog.infra.openai

import com.example.lifelog.config.OpenAiProperties
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class OpenAiClient(
    props: OpenAiProperties,
) {
    private val client: WebClient =
        WebClient
            .builder()
            .baseUrl(props.baseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer ${props.apiKey}")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()

    private val model = props.model
    private val timeout = Duration.ofSeconds(props.timeoutSeconds)

    private val objectMapper = ObjectMapper()

    /**
     * 기존 단순 구조화 호출
     */
    fun structure(
        system: String,
        user: String,
    ): String {
        val req =
            ChatCompletionsRequest(
                model = model,
                messages =
                    listOf(
                        Message(role = "system", content = system),
                        Message(role = "user", content = user),
                    ),
                temperature = 0.2,
            )

        return client
            .post()
            .uri("/chat/completions")
            .bodyValue(req)
            .retrieve()
            .bodyToMono(ChatCompletionsResponse::class.java)
            .timeout(timeout)
            .onErrorResume { e -> Mono.error(RuntimeException("OpenAI 호출 실패: ${e.message}", e)) }
            .block()!!
            .choices
            .first()
            .message
            .content
    }

    /**
     * Structured Outputs API 호출 (JSON Schema 포함)
     */
    fun structureWithSchema(
        system: String,
        user: String,
        schemaJson: String,
    ): String {
        // JSON Schema 파싱 → Map 으로 변환
        val schemaMap: Any =
            objectMapper.readValue(schemaJson, Any::class.java)

        val responseFormat =
            mapOf(
                "type" to "json_schema",
                "json_schema" to
                    mapOf(
                        "name" to "structured_event_list",
                        "strict" to true,
                        "schema" to schemaMap,
                    ),
            )

        val req =
            ChatCompletionsRequest(
                model = model,
                messages =
                    listOf(
                        Message(role = "system", content = system),
                        Message(role = "user", content = user),
                    ),
                temperature = 0.2,
                // response_format = responseFormat
            )

        return client
            .post()
            .uri("/chat/completions")
            .bodyValue(req)
            .retrieve()
            .bodyToMono<ChatCompletionsResponse>()
            .timeout(timeout)
            .onErrorResume { e -> Mono.error(RuntimeException("OpenAI 호출 실패: ${e.message}", e)) }
            .block()!!
            .choices
            .first()
            .message
            .content
    }

    // 리소스에서 JSON Schema 파일 로딩
    fun loadJsonSchema(resourcePath: String): String =
        javaClass
            .getResource(resourcePath)!!
            .readText()

    data class ChatCompletionsRequest(
        val model: String,
        val messages: List<Message>,
        val temperature: Double? = null,
        val response_format: Map<String, Any>? = null,
    )

    data class Message(
        val role: String,
        val content: String,
    )

    data class ChatCompletionsResponse(
        val choices: List<Choice>,
    )

    data class Choice(
        val message: Message,
    )
}
