package com.example.lifelog.presentation.api.apple

import com.example.lifelog.application.apple.HandleAppleNotificationUseCase
import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 애플 서버-to-서버 알림 엔드포인트
 * 한국 기반 개발자는 2026년 1월 1일부터 필수
 *
 * 애플 개발자 콘솔에서 이 엔드포인트를 등록해야 함:
 * - Capabilities > Sign in with Apple > Server-to-Server Notification URL
 */
@RestController
@RequestMapping("/apple/notifications")
class AppleNotificationController(
    private val handleAppleNotificationUseCase: HandleAppleNotificationUseCase,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 애플 서버-to-서버 알림 수신 엔드포인트
     * POST 요청으로 JWT가 포함된 JSON을 받음
     *
     * 요청 형식:
     * {
     *   "payload": "<JWT>"
     * }
     */
    @PostMapping
    fun handleNotification(
        @RequestBody request: AppleNotificationRequest,
    ): ResponseEntity<Unit> {
        log.info("[APPLE_NOTIFICATION] Received notification request")

        try {
            val jwtPayload = request.payload
            if (jwtPayload.isBlank()) {
                log.warn("[APPLE_NOTIFICATION] Empty payload received")
                return ResponseEntity.badRequest().build()
            }

            handleAppleNotificationUseCase.execute(jwtPayload)
            return ResponseEntity.ok().build()
        } catch (e: Exception) {
            log.error("[APPLE_NOTIFICATION] Failed to process notification", e)
            // 애플은 200 응답을 기대하므로, 내부 오류가 있어도 200을 반환
            // (애플이 재시도할 수 있도록)
            return ResponseEntity.ok().build()
        }
    }
}

/**
 * 애플 서버-to-서버 알림 요청 DTO
 */
data class AppleNotificationRequest(
    @JsonProperty("payload")
    val payload: String,
)
