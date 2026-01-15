package com.example.lifelog.presentation.api.insight

import jakarta.validation.constraints.NotNull

data class InsightSettingsUpsertRequest(
    @field:NotNull
    var enabled: Boolean?,
)

data class InsightSettingsResponse(
    val enabled: Boolean,
)
