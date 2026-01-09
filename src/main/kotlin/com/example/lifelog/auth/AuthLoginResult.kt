package com.example.lifelog.auth

data class OAuthAccessTokenLoginRequest(
    val accessToken: String,
)

data class AuthLoginResult(
    val accessToken: String,
    val refreshToken: String,
    val displayName: String,
    val isNewUser: Boolean,
)
