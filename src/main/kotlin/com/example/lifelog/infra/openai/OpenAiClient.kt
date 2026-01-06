package com.example.lifelog.infra.openai

import com.example.lifelog.config.OpenAiProperties
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.security.MessageDigest
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.min
import kotlin.random.Random

@Component
class OpenAiClient(
    props: OpenAiProperties,
    private val objectMapper: ObjectMapper,
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

    private val apiSemaphore = Semaphore(1)
    private val cooldownUntilRef = AtomicReference<Instant?>(null)
    private val consecutive429 = AtomicInteger(0)

    private data class CacheEntry(
        val value: String,
        val createdAt: Instant,
    )

    private val cache = ConcurrentHashMap<String, CacheEntry>()
    private val cacheTtl = Duration.ofMinutes(10)

    private val maxAttempts = 2
    private val backoffCapSeconds = 4L
    private val baseBackoffSeconds = 1L

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

        val ck = cacheKey(kind = "plain", system = system, user = user, schemaJson = null)
        return callChatCompletions(req, ck)
    }

    /**
     * schemaJson can be either:
     * 1) Plain JSON schema: { "type": "object", ... }
     * 2) Envelope: { "name": "...", "strict": true, "schema": { "type": "object", ... } }
     */
    fun structureWithSchema(
        system: String,
        user: String,
        schemaJson: String,
    ): String {
        val schemaNode: JsonNode = objectMapper.readTree(schemaJson)

        val (schemaName, strict, effectiveSchema) = unwrapSchemaEnvelope(schemaNode)

        val responseFormat =
            mapOf(
                "type" to "json_schema",
                "json_schema" to
                    mapOf(
                        "name" to schemaName,
                        "strict" to strict,
                        "schema" to objectMapper.convertValue(effectiveSchema, Any::class.java),
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
                response_format = responseFormat,
            )

        val ck = cacheKey(kind = "schema", system = system, user = user, schemaJson = schemaJson)
        return callChatCompletions(req, ck)
    }

    private fun unwrapSchemaEnvelope(node: JsonNode): Triple<String, Boolean, JsonNode> {
        if (node.has("schema")) {
            val schema = node.get("schema")
            val name = node.get("name")?.asText()?.takeIf { it.isNotBlank() } ?: "response"
            val strict = node.get("strict")?.asBoolean() ?: true
            return Triple(name, strict, schema)
        }

        return Triple("response", true, node)
    }

    private fun callChatCompletions(
        req: ChatCompletionsRequest,
        cacheKey: String,
    ): String {
        cache[cacheKey]?.let { entry ->
            if (!isExpired(entry)) return entry.value
            cache.remove(cacheKey)
        }

        val now = Instant.now()
        val cooldownUntil = cooldownUntilRef.get()
        if (cooldownUntil != null && now.isBefore(cooldownUntil)) {
            throw OpenAiRateLimitedException(cooldownUntil = cooldownUntil)
        }

        val acquired = apiSemaphore.tryAcquire(1, 750, TimeUnit.MILLISECONDS)
        if (!acquired) throw OpenAiBusyException()

        try {
            val content = withShortRetry { executeOnce(req) }
            consecutive429.set(0)
            cache[cacheKey] = CacheEntry(value = content, createdAt = Instant.now())
            return content
        } finally {
            apiSemaphore.release()
        }
    }

    private fun executeOnce(req: ChatCompletionsRequest): String =
        try {
            client
                .post()
                .uri("/chat/completions")
                .bodyValue(req)
                .exchangeToMono { resp ->
                    val code = resp.statusCode().value()

                    if (code == 429) {
                        val retryAfterSeconds =
                            resp
                                .headers()
                                .asHttpHeaders()
                                .getFirst("Retry-After")
                                ?.toLongOrNull()
                        val cooldownUntil = computeCooldownUntil(retryAfterSeconds)
                        setCooldown(cooldownUntil)
                        Mono.error(OpenAiRateLimitedException(cooldownUntil = cooldownUntil))
                    } else if (code in 500..599 || code == 408 || code == 409) {
                        resp
                            .bodyToMono(String::class.java)
                            .defaultIfEmpty("")
                            .flatMap { body ->
                                Mono.error(OpenAiTransientException("OpenAI transient http=$code bodySnippet=${body.take(300)}"))
                            }
                    } else if (code in 400..499) {
                        resp
                            .bodyToMono(String::class.java)
                            .defaultIfEmpty("")
                            .flatMap { body ->
                                Mono.error(OpenAiPermanentException("OpenAI permanent http=$code bodySnippet=${body.take(500)}"))
                            }
                    } else {
                        resp.bodyToMono(ChatCompletionsResponse::class.java)
                    }
                }.timeout(timeout)
                .map { parsed ->
                    val choice =
                        parsed.choices.firstOrNull()
                            ?: throw OpenAiTransientException("OpenAI response has empty choices")
                    val content = choice.message.content
                    if (content.isBlank()) throw OpenAiTransientException("OpenAI response content is blank")
                    content
                }.block()!!
        } catch (e: OpenAiException) {
            throw e
        } catch (e: WebClientResponseException) {
            if (e.statusCode.value() == 429) {
                val cooldownUntil = computeCooldownUntil(null)
                setCooldown(cooldownUntil)
                throw OpenAiRateLimitedException(cooldownUntil = cooldownUntil, cause = e)
            }
            throw OpenAiTransientException("OpenAI 호출 실패: ${e.message}", e)
        } catch (e: Exception) {
            throw OpenAiTransientException("OpenAI 호출 실패: ${e.message}", e)
        }

    private fun isExpired(entry: CacheEntry): Boolean = Duration.between(entry.createdAt, Instant.now()) > cacheTtl

    // 리소스에서 JSON Schema 파일 로딩
    fun loadJsonSchema(resourcePath: String): String = javaClass.getResource(resourcePath)!!.readText()

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

    private fun <T> withShortRetry(block: () -> T): T {
        var attempt = 0
        var last: Throwable? = null

        while (attempt < maxAttempts) {
            try {
                return block()
            } catch (t: Throwable) {
                last = t

                if (t is OpenAiRateLimitedException) throw t
                if (t is OpenAiBusyException) throw t
                if (t is OpenAiPermanentException) throw t
                if (t !is OpenAiTransientException) throw t

                attempt += 1
                if (attempt >= maxAttempts) break

                val delaySeconds = computeBackoffSeconds(attempt)
                Thread.sleep(delaySeconds * 1000L)
            }
        }

        throw OpenAiTransientException(
            message = "OpenAI 호출 재시도 초과 (attempts=$maxAttempts). last=${last?.message}",
            cause = last,
        )
    }

    private fun computeBackoffSeconds(attempt: Int): Long {
        val cappedAttempt = min(attempt, 3)
        val raw = baseBackoffSeconds * (1L shl (cappedAttempt - 1))
        val capped = min(raw, backoffCapSeconds)
        return if (capped <= 1L) capped else Random.nextLong(0, capped + 1)
    }

    private fun computeCooldownUntil(retryAfterSeconds: Long?): Instant {
        val now = Instant.now()
        if (retryAfterSeconds != null && retryAfterSeconds > 0) return now.plusSeconds(retryAfterSeconds)

        val c = consecutive429.incrementAndGet()
        val base =
            when {
                c <= 1 -> 120L
                c == 2 -> 300L
                else -> 900L
            }

        val jitter = Random.nextLong(0, 30)
        return now.plusSeconds(base + jitter)
    }

    private fun setCooldown(newUntil: Instant) {
        cooldownUntilRef.updateAndGet { cur ->
            if (cur == null) newUntil else maxOf(cur, newUntil)
        }
    }

    /**
     * schemaJson 전체 문자열을 cacheKey에 넣지 말고 schemaHash만 포함(성능/메모리).
     */
    private fun cacheKey(
        kind: String,
        system: String,
        user: String,
        schemaJson: String?,
    ): String {
        val systemHash = sha256Hex(system)
        val userHash = sha256Hex(user)
        val schemaHash = schemaJson?.let { sha256Hex(it) }

        val raw =
            buildString {
                append("model=").append(model).append('\n')
                append("kind=").append(kind).append('\n')
                append("systemHash=").append(systemHash).append('\n')
                append("userHash=").append(userHash).append('\n')
                if (schemaHash != null) append("schemaHash=").append(schemaHash)
            }
        return sha256Hex(raw)
    }

    private fun sha256Hex(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        val sb = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            sb.append(((b.toInt() shr 4) and 0xF).toString(16))
            sb.append((b.toInt() and 0xF).toString(16))
        }
        return sb.toString()
    }
}
