package com.example.lifelog.infra.openai

import java.time.Instant

sealed class OpenAiException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

class OpenAiBusyException(
    message: String = "OpenAI semaphore busy (concurrency=1). Please retry later.",
) : OpenAiException(message)

class OpenAiRateLimitedException(
    val cooldownUntil: Instant,
    message: String = "OpenAI rate limited (429). cooldownUntil=$cooldownUntil",
    cause: Throwable? = null,
) : OpenAiException(message, cause)

class OpenAiTransientException(
    message: String,
    cause: Throwable? = null,
) : OpenAiException(message, cause)

class OpenAiPermanentException(
    message: String,
    cause: Throwable? = null,
) : OpenAiException(message, cause)
