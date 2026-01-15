package com.example.lifelog.push.fcm

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "lifelog.push.fcm")
data class FcmProperties(
    var enabled: Boolean = false,
    var serviceAccountPath: String = "",
)
