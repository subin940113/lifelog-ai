package com.example.lifelog.presentation.api.log

import com.example.lifelog.application.log.CreateLogUseCase
import com.example.lifelog.application.log.ListLogsUseCase
import com.example.lifelog.infrastructure.security.AuthPrincipal
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

/**
 * 로그 API Controller
 */
@RestController
@RequestMapping("/api/logs")
class LogController(
    private val createLogUseCase: CreateLogUseCase,
    private val listLogsUseCase: ListLogsUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @Valid @RequestBody request: CreateLogRequest,
    ): CreateLogResponse {
        val savedLog = createLogUseCase.execute(
            userId = principal.userId,
            content = request.content,
        )
        return CreateLogResponse(logId = savedLog.id)
    }

    @GetMapping
    fun list(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @RequestParam(required = false, defaultValue = "50") limit: Int,
        @RequestParam(required = false) cursor: String?,
    ) = listLogsUseCase.execute(
        userId = principal.userId,
        limit = limit,
        cursor = cursor,
    )
}

/**
 * 로그 생성 요청 DTO
 */
data class CreateLogRequest(
    val content: String,
)

/**
 * 로그 생성 응답 DTO
 */
data class CreateLogResponse(
    val logId: Long,
)
