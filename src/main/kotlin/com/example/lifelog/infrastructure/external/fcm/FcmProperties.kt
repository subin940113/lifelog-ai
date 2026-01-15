package com.example.lifelog.infrastructure.external.fcm

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * FCM 설정 프로퍼티
 */
@ConfigurationProperties(prefix = "lifelog.push.fcm")
data class FcmProperties(
    var enabled: Boolean = false,
    var serviceAccountPath: String = "",
)
