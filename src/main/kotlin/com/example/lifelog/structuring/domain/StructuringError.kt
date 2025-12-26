package com.example.lifelog.structuring.domain

sealed class StructuringError(
    open val message: String,
) {
    data class InvalidInput(
        override val message: String,
    ) : StructuringError(message)

    data class ProviderUnavailable(
        override val message: String,
    ) : StructuringError(message)

    data class ProviderTimeout(
        override val message: String,
    ) : StructuringError(message)

    data class ParseFailed(
        override val message: String,
        val rawOutput: String? = null,
    ) : StructuringError(message)

    data class PolicyBlocked(
        override val message: String,
    ) : StructuringError(message)

    data class ExternalFailure(
        override val message: String,
        val cause: Throwable? = null,
    ) : StructuringError(message)

    data class Unknown(
        override val message: String,
        val cause: Throwable? = null,
    ) : StructuringError(message)
}
