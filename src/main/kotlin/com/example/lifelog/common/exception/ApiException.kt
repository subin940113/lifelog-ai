package com.example.lifelog.common.exception

/**
 * API 예외 기본 클래스
 */
open class ApiException(
    val errorCode: ErrorCode,
    message: String? = null,
    cause: Throwable? = null,
) : RuntimeException(message ?: errorCode.message, cause) {
    val code: String
        get() = errorCode.code

    val httpStatus: org.springframework.http.HttpStatus
        get() = errorCode.httpStatus
}

/**
 * 비즈니스 로직 예외
 */
class BusinessException(
    errorCode: ErrorCode,
    message: String? = null,
    cause: Throwable? = null,
) : ApiException(errorCode, message, cause)

/**
 * 검증 예외
 */
class ValidationException(
    errorCode: ErrorCode,
    message: String? = null,
    cause: Throwable? = null,
) : ApiException(errorCode, message, cause)

/**
 * 리소스 없음 예외
 */
class NotFoundException(
    errorCode: ErrorCode,
    message: String? = null,
    cause: Throwable? = null,
) : ApiException(errorCode, message, cause)

/**
 * 인증/인가 예외
 */
class UnauthorizedException(
    errorCode: ErrorCode = ErrorCode.UNAUTHORIZED,
    message: String? = null,
    cause: Throwable? = null,
) : ApiException(errorCode, message, cause)

/**
 * 권한 없음 예외
 */
class ForbiddenException(
    errorCode: ErrorCode = ErrorCode.FORBIDDEN,
    message: String? = null,
    cause: Throwable? = null,
) : ApiException(errorCode, message, cause)
