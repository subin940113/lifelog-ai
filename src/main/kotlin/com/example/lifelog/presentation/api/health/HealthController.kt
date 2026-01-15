package com.example.lifelog.presentation.api.health

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 헬스 체크 API Controller
 */
@RestController
@RequestMapping("/health")
class HealthController {
    @GetMapping
    fun health() = HttpStatus.OK
}
