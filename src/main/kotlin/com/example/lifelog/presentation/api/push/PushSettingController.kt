package com.example.lifelog.presentation.api.push

import com.example.lifelog.application.push.GetPushSettingUseCase
import com.example.lifelog.application.push.UpdatePushSettingUseCase
import com.example.lifelog.infrastructure.security.AuthPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 푸시 설정 API Controller
 */
@RestController
@RequestMapping("/api/push/settings")
class PushSettingController(
    private val getPushSettingUseCase: GetPushSettingUseCase,
    private val updatePushSettingUseCase: UpdatePushSettingUseCase,
) {
    @GetMapping
    fun get(
        @AuthenticationPrincipal principal: AuthPrincipal,
    ): PushSettingResponse {
        val userId = principal.userId
        val pushSetting = getPushSettingUseCase.execute(userId)
        return PushSettingResponse(enabled = pushSetting.enabled)
    }

    @PutMapping
    fun update(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @RequestBody request: PushSettingUpdateRequest,
    ): PushSettingResponse {
        val userId = principal.userId
        val pushSetting = updatePushSettingUseCase.execute(userId, request.enabled)
        return PushSettingResponse(enabled = pushSetting.enabled)
    }
}

/**
 * 푸시 설정 응답 DTO
 */
data class PushSettingResponse(
    val enabled: Boolean,
)

/**
 * 푸시 설정 업데이트 요청 DTO
 */
data class PushSettingUpdateRequest(
    val enabled: Boolean,
)
