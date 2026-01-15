package com.example.lifelog.presentation.api.home

import com.example.lifelog.application.home.GetHomeUseCase
import com.example.lifelog.application.home.HomeResponse
import com.example.lifelog.infrastructure.security.AuthPrincipal
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import kotlin.math.max
import kotlin.math.min

/**
 * 홈 화면 API Controller
 */
@RestController
@RequestMapping("/api/home")
class HomeController(
    private val getHomeUseCase: GetHomeUseCase,
) {
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getHome(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @RequestParam(required = false, defaultValue = "day") period: String,
        @RequestParam(required = false, defaultValue = "3") limitLogs: Int,
        @RequestParam(required = false, defaultValue = "2") limitInsights: Int,
    ): HomeResponse {
        val safeLimitLogs = min(max(limitLogs, 1), 20)
        val safeLimitInsights = min(max(limitInsights, 0), 20)

        return getHomeUseCase.execute(
            userId = principal.userId,
            period = period, // 현재는 호환용으로 받기만
            limitLogs = safeLimitLogs,
            limitInsights = safeLimitInsights,
        )
    }
}
