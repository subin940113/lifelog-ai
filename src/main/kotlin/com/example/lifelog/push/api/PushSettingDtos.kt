package com.example.lifelog.push.api

data class PushSettingResponse(
    val enabled: Boolean,
)

data class PushSettingUpdateRequest(
    val enabled: Boolean,
)
