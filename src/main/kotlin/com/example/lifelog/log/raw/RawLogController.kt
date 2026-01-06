package com.example.lifelog.log.raw

import com.example.lifelog.auth.security.AuthPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/logs")
class RawLogController(
    private val rawLogService: RawLogService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @Valid @RequestBody req: CreateLogRequest,
    ): CreateLogResponse {
        val saved =
            rawLogService.create(
                userId = principal.userId,
                content = req.content,
            )
        return CreateLogResponse(logId = saved.id)
    }

    @GetMapping
    fun list(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @RequestParam(required = false, defaultValue = "50") limit: Int,
        @RequestParam(required = false) cursor: String?,
    ): LogsPageResponse =
        rawLogService.list(
            userId = principal.userId,
            limit = limit,
            cursor = cursor,
        )
}
