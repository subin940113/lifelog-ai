package com.example.lifelog.push.api

import com.example.lifelog.auth.security.AuthPrincipal
import com.example.lifelog.push.PushSettingService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/push/settings")
class PushSettingController(
    private val service: PushSettingService,
) {
    @GetMapping
    fun get(
        @AuthenticationPrincipal principal: AuthPrincipal,
    ): PushSettingResponse {
        val userId = principal.userId
        val s = service.get(userId)
        return PushSettingResponse(enabled = s.enabled)
    }

    @PutMapping
    fun update(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @RequestBody req: PushSettingUpdateRequest,
    ): PushSettingResponse {
        val userId = principal.userId
        val s = service.setEnabled(userId, req.enabled)
        return PushSettingResponse(enabled = s.enabled)
    }
}
