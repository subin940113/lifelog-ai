package com.example.lifelog.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

/**
 * WebClient 설정
 */
@Configuration
class WebClientConfig {
    @Bean
    fun webClient(): WebClient = WebClient.builder().build()
}
