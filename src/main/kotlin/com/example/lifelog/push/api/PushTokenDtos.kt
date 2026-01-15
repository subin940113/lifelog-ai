package com.example.lifelog.push.api

data class RegisterPushTokenRequest(
    val token: String?,
    val platform: String? = null, // optional: "android" | "ios"
)

data class RegisterPushTokenResponse(
    val ok: Boolean = true,
)
