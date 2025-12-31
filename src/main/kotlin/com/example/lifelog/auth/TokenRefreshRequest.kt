package com.example.lifelog.auth

data class TokenRefreshRequest(
    val refreshToken: String,
)

data class TokenRefreshResponse(
    val accessToken: String,
    val refreshToken: String,
)

data class OAuthLoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val isNewUser: Boolean,
)
