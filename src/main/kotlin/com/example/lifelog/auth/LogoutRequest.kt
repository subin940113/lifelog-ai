package com.example.lifelog.auth

import jakarta.validation.constraints.NotBlank

data class LogoutRequest(
    @field:NotBlank val refreshToken: String,
    val allDevices: Boolean = false,
)
