package com.example.lifelog.structuring.impl

import com.example.lifelog.structuring.domain.StructuringRequest
import com.example.lifelog.structuring.domain.StructuringResult
import com.example.lifelog.structuring.port.EventStructurer
import org.springframework.stereotype.Component

@Component
class LlmEventStructurer : EventStructurer {
    override fun structure(request: StructuringRequest): StructuringResult {
        // TODO: OpenAI 연동 시 구현
        throw UnsupportedOperationException("LLM structurer not implemented yet")
    }
}
