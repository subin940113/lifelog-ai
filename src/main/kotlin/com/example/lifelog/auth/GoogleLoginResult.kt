package com.example.lifelog.auth

data class GoogleLoginResult(
    val accessToken: String,
    val displayName: String,
    val isNewUser: Boolean,
)
