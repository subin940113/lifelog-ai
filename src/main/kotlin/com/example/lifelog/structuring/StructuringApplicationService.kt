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
    fun createStructuredEvent(request: StructuringRequest) {
        // MVP: 구조화 결과 저장/실패 기록은 기존 RawLog 생성 플로우에서 처리 중.
        //       (LLM 연동 단계에서 저장 책임을 이 서비스로 이관할 수 있음)
        structurer.structure(request)
    }
}
