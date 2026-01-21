package com.example.lifelog.common.exception

import org.springframework.http.HttpStatus

/**
 * API 오류 코드
 */
enum class ErrorCode(
    val code: String,
    val message: String,
    val httpStatus: HttpStatus,
) {
    // 인증/인가 관련 (401, 403)
    UNAUTHORIZED("AUTH_001", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED_REFRESH_TOKEN_NOT_FOUND("AUTH_002", "리프레시 토큰을 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED_REFRESH_TOKEN_REVOKED("AUTH_003", "리프레시 토큰이 취소되었습니다.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED_REFRESH_TOKEN_EXPIRED("AUTH_004", "리프레시 토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("AUTH_005", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // 검증 관련 (400)
    VALIDATION_REQUIRED("VALID_001", "필수 값이 누락되었습니다.", HttpStatus.BAD_REQUEST),
    VALIDATION_INVALID_FORMAT("VALID_002", "잘못된 형식입니다.", HttpStatus.BAD_REQUEST),
    VALIDATION_OUT_OF_RANGE("VALID_003", "허용 범위를 벗어났습니다.", HttpStatus.BAD_REQUEST),
    VALIDATION_BLANK_CONTENT("VALID_004", "내용이 비어있습니다.", HttpStatus.BAD_REQUEST),
    VALIDATION_BLANK_KEYWORD("VALID_005", "키워드가 비어있습니다.", HttpStatus.BAD_REQUEST),
    VALIDATION_BLANK_TOKEN("VALID_006", "토큰이 비어있습니다.", HttpStatus.BAD_REQUEST),
    VALIDATION_INVALID_BUCKET_MINUTES("VALID_007", "버킷 분 단위가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),

    // 리소스 없음 (404)
    NOT_FOUND_USER("NOTFOUND_001", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NOT_FOUND_INSIGHT("NOTFOUND_002", "인사이트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NOT_FOUND_RESOURCE("NOTFOUND_003", "리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NOT_FOUND_WATER_DROP("NOTFOUND_004", "물방울을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 비즈니스 로직 위반 (400, 409)
    BUSINESS_MAX_INTERESTS_EXCEEDED("BIZ_001", "관심사는 최대 5개까지 등록할 수 있습니다.", HttpStatus.BAD_REQUEST),
    BUSINESS_USER_ALREADY_DELETED("BIZ_002", "이미 탈퇴한 사용자입니다.", HttpStatus.BAD_REQUEST),
    BUSINESS_OAUTH_PROVIDER_ERROR("BIZ_003", "OAuth 제공자 오류가 발생했습니다.", HttpStatus.BAD_REQUEST),
    BUSINESS_OAUTH_UNAUTHORIZED("BIZ_004", "OAuth 인증에 실패했습니다.", HttpStatus.UNAUTHORIZED),

    // 외부 서비스 오류 (502, 503)
    EXTERNAL_OPENAI_ERROR("EXT_001", "OpenAI 서비스 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY),
    EXTERNAL_OPENAI_RATE_LIMITED("EXT_002", "OpenAI 요청 한도가 초과되었습니다.", HttpStatus.TOO_MANY_REQUESTS),
    EXTERNAL_OPENAI_BUSY("EXT_003", "OpenAI 서비스가 일시적으로 사용 불가능합니다.", HttpStatus.SERVICE_UNAVAILABLE),
    EXTERNAL_FCM_ERROR("EXT_004", "FCM 서비스 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY),

    // 내부 서버 오류 (500)
    INTERNAL_ERROR("INTERNAL_001", "서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INTERNAL_UNEXPECTED("INTERNAL_002", "예상치 못한 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
}
