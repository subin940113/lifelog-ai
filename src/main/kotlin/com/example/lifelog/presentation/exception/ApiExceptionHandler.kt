package com.example.lifelog.presentation.exception

import com.example.lifelog.common.exception.ApiException
import com.example.lifelog.common.exception.ErrorCode
import com.example.lifelog.infrastructure.external.openai.OpenAiBusyException
import com.example.lifelog.infrastructure.external.openai.OpenAiException
import com.example.lifelog.infrastructure.external.openai.OpenAiPermanentException
import com.example.lifelog.infrastructure.external.openai.OpenAiRateLimitedException
import com.example.lifelog.infrastructure.external.openai.OpenAiTransientException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * API 예외 핸들러
 */
@RestControllerAdvice
class ApiExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(ApiException::class)
    fun handleApiException(e: ApiException): ResponseEntity<ErrorResponse> {
        log.debug("API Exception: code={}, message={}", e.code, e.message, e)
        return ResponseEntity
            .status(e.httpStatus)
            .body(ErrorResponse(code = e.code, message = e.message ?: e.errorCode.message))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        log.debug("IllegalArgumentException: {}", e.message, e)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(code = ErrorCode.VALIDATION_REQUIRED.code, message = e.message ?: ErrorCode.VALIDATION_REQUIRED.message))
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(e: IllegalStateException): ResponseEntity<ErrorResponse> {
        log.debug("IllegalStateException: {}", e.message, e)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(code = ErrorCode.INTERNAL_ERROR.code, message = e.message ?: ErrorCode.INTERNAL_ERROR.message))
    }

    @ExceptionHandler(OpenAiRateLimitedException::class)
    fun handleOpenAiRateLimitedException(e: OpenAiRateLimitedException): ResponseEntity<ErrorResponse> {
        log.warn("OpenAI Rate Limited: cooldownUntil={}", e.cooldownUntil, e)
        return ResponseEntity
            .status(ErrorCode.EXTERNAL_OPENAI_RATE_LIMITED.httpStatus)
            .body(
                ErrorResponse(code = ErrorCode.EXTERNAL_OPENAI_RATE_LIMITED.code, message = ErrorCode.EXTERNAL_OPENAI_RATE_LIMITED.message),
            )
    }

    @ExceptionHandler(OpenAiBusyException::class)
    fun handleOpenAiBusyException(e: OpenAiBusyException): ResponseEntity<ErrorResponse> {
        log.warn("OpenAI Busy: {}", e.message, e)
        return ResponseEntity
            .status(ErrorCode.EXTERNAL_OPENAI_BUSY.httpStatus)
            .body(ErrorResponse(code = ErrorCode.EXTERNAL_OPENAI_BUSY.code, message = ErrorCode.EXTERNAL_OPENAI_BUSY.message))
    }

    @ExceptionHandler(OpenAiPermanentException::class)
    fun handleOpenAiPermanentException(e: OpenAiPermanentException): ResponseEntity<ErrorResponse> {
        log.error("OpenAI Permanent Error: {}", e.message, e)
        return ResponseEntity
            .status(ErrorCode.EXTERNAL_OPENAI_ERROR.httpStatus)
            .body(ErrorResponse(code = ErrorCode.EXTERNAL_OPENAI_ERROR.code, message = ErrorCode.EXTERNAL_OPENAI_ERROR.message))
    }

    @ExceptionHandler(OpenAiTransientException::class)
    fun handleOpenAiTransientException(e: OpenAiTransientException): ResponseEntity<ErrorResponse> {
        log.warn("OpenAI Transient Error: {}", e.message, e)
        return ResponseEntity
            .status(ErrorCode.EXTERNAL_OPENAI_ERROR.httpStatus)
            .body(ErrorResponse(code = ErrorCode.EXTERNAL_OPENAI_ERROR.code, message = ErrorCode.EXTERNAL_OPENAI_ERROR.message))
    }

    @ExceptionHandler(OpenAiException::class)
    fun handleOpenAiException(e: OpenAiException): ResponseEntity<ErrorResponse> {
        log.error("OpenAI Exception: {}", e.message, e)
        return ResponseEntity
            .status(ErrorCode.EXTERNAL_OPENAI_ERROR.httpStatus)
            .body(ErrorResponse(code = ErrorCode.EXTERNAL_OPENAI_ERROR.code, message = ErrorCode.EXTERNAL_OPENAI_ERROR.message))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unexpected Exception: {}", e.message, e)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(code = ErrorCode.INTERNAL_UNEXPECTED.code, message = ErrorCode.INTERNAL_UNEXPECTED.message))
    }
}

/**
 * 오류 응답 DTO
 */
data class ErrorResponse(
    val code: String,
    val message: String,
)
