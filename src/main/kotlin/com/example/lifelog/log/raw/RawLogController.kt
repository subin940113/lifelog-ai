package com.example.lifelog.log.raw

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/logs")
class RawLogController(
    private val rawLogRepository: RawLogRepository
) {

    data class CreateLogRequest(
        @field:NotBlank(message = "content is required")
        val content: String
    )

    data class CreateLogResponse(
        val logId: Long,
        val status: String = "CREATED"
    )

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody req: CreateLogRequest): CreateLogResponse {
        // MVP: OAuth2 전이므로 임시 userId
        val userId = 1L

        val saved = rawLogRepository.save(
            RawLog(
                userId = userId,
                content = req.content.trim()
            )
        )
        return CreateLogResponse(logId = saved.id)
    }
}