package com.example.lifelog.structuring

import com.example.lifelog.structuring.domain.StructuringRequest
import com.example.lifelog.structuring.port.EventStructurer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StructuringApplicationService(
    @Qualifier("eventStructurer") private val structurer: EventStructurer,
) {
    @Transactional
    fun createStructuredEvent(request: StructuringRequest) = structurer.structure(request)
}
