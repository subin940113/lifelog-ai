package com.example.lifelog.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "lifelog")
data class LifeLogProperties(
    val structurer: String,
)
