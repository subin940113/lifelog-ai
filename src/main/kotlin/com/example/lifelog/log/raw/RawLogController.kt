package com.example.lifelog.log.raw

import com.example.lifelog.auth.security.AuthPrincipal
import com.example.lifelog.log.event.RawLogCreatedEvent
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/logs")
class RawLogController(
    private val rawLogRepository: RawLogRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    data class CreateLogRequest(
        @field:NotBlank(message = "content is required")
        val content: String,
    )

    data class CreateLogResponse(
        val logId: Long,
        val status: String = "CREATED",
    )

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @Valid @RequestBody req: CreateLogRequest,
    ): CreateLogResponse {
        val userId = principal.userId

        val saved =
            rawLogRepository.save(
                RawLog(
                    userId = userId,
                    content = req.content.trim(),
                ),
            )

        eventPublisher.publishEvent(RawLogCreatedEvent(saved))

        return CreateLogResponse(logId = saved.id)
    }
}
