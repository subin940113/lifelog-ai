package com.example.lifelog.application.insight.pipeline

import com.example.lifelog.domain.insight.InsightCreatedEvent
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
