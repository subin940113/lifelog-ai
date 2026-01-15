package com.example.lifelog.insight.pipeline

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class InsightEventPublisher(
    private val publisher: ApplicationEventPublisher,
) {
    fun publishInsightCreated(event: InsightCreatedEvent) {
        publisher.publishEvent(event)
    }
}
