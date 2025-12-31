package com.example.lifelog.auth

data class GoogleLoginResult(
    val accessToken: String,
    val isNewUser: Boolean,
)


