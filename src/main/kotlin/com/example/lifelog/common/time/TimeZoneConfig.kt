package com.example.lifelog.common.time

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.time.ZoneId

/**
 * 애플리케이션 전역 타임존 설정
 */
@Configuration
@ConfigurationProperties(prefix = "lifelog.time")
data class TimeZoneConfig(
    val defaultZone: String = "Asia/Seoul",
) {
    val defaultZoneId: ZoneId
        get() = ZoneId.of(defaultZone)
}
