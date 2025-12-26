package com.example.lifelog.config

import com.example.lifelog.structuring.impl.LlmEventStructurer
import com.example.lifelog.structuring.impl.PseudoEventStructurer
import com.example.lifelog.structuring.port.EventStructurer
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(LifeLogProperties::class, OpenAiProperties::class)
class StructuringConfig(
    private val props: LifeLogProperties,
) {
    @Bean
    fun eventStructurer(
        pseudo: PseudoEventStructurer,
        openai: LlmEventStructurer,
    ): EventStructurer =
        when (props.structurer) {
            "openai" -> openai
            else -> pseudo
        }
}
