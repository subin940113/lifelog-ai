package com.example.lifelog.interest

import jakarta.validation.constraints.NotBlank

data class InterestStateResponse(
    val keywords: List<String>,
)

data class InterestKeywordRequest(
    @field:NotBlank
    val keyword: String?,
)
