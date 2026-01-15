package com.example.lifelog.infrastructure.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * 인사이트 파이프라인 설정
 */
@Configuration
@EnableConfigurationProperties(InsightPolicyProperties::class)
class InsightPipelineConfig
