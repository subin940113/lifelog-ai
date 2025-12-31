package com.example.lifelog.auth

import jakarta.validation.constraints.NotBlank

data class GoogleLoginRequest(
    @field:NotBlank val idToken: String,
)
