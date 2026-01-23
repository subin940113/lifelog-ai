package com.example.lifelog.presentation.api.auth

import jakarta.validation.constraints.NotBlank

data class GoogleLoginRequest(
    @field:NotBlank val idToken: String,
)

data class OAuthAccessTokenLoginRequest(
    @field:NotBlank val accessToken: String,
)

data class AppleLoginRequest(
    @field:NotBlank val authorizationCode: String,
)

data class LogoutRequest(
    @field:NotBlank val refreshToken: String,
    val allDevices: Boolean = false,
)
