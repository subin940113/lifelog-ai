package com.example.lifelog.user

import java.time.Instant

data class UserMeResponse(
    val id: Long,
    val displayName: String?,
    val createdAt: Instant,
)

data class UpdateUserMeRequest(
    val displayName: String? = null,
)