package com.example.lifelog.auth

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oauth.google")
data class GoogleOAuthProperties(
    var clientIds: List<String> = emptyList(),
)