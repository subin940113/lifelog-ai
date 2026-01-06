package com.example.lifelog.insight.settings

import jakarta.validation.constraints.NotNull

data class InsightSettingsResponse(
    val enabled: Boolean,
)

data class InsightSettingsUpsertRequest(
    @field:NotNull
    var enabled: Boolean?,
)
