package com.example.lifelog.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Apple OAuth 설정 Properties
 */
@ConfigurationProperties(prefix = "oauth.apple")
data class AppleOAuthProperties(
    var clientId: String = "",
    var teamId: String = "",
    var keyId: String = "",
    var privateKey: String = "", // P8 파일 내용
)
