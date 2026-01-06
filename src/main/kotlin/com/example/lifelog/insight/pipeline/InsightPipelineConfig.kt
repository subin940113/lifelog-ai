package com.example.lifelog.insight.pipeline

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(InsightPolicyProperties::class)
class InsightPipelineConfig
