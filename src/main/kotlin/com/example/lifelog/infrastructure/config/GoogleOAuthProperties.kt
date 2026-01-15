package com.example.lifelog.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Google OAuth 설정 Properties
 */
@ConfigurationProperties(prefix = "oauth.google")
data class GoogleOAuthProperties(
    var clientIds: List<String> = emptyList(),
)
