package com.example.lifelog.interest

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class InterestStateResponse(
    val enabled: Boolean,
    val keywords: List<String>,
)

data class InterestEnabledRequest(
    @field:NotNull
    val enabled: Boolean?,
)

data class InterestKeywordRequest(
    @field:NotBlank
    val keyword: String?,
)
