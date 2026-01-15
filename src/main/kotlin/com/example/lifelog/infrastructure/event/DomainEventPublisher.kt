package com.example.lifelog.infrastructure.event

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

/**
 * 도메인 이벤트 발행자
 * Infrastructure 레이어에서 도메인 이벤트를 Spring 이벤트로 변환
 */
@Component
class DomainEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    fun publish(event: Any) {
        applicationEventPublisher.publishEvent(event)
    }
}
