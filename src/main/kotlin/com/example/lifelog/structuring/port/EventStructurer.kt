package com.example.lifelog.structuring.port

import com.example.lifelog.structuring.domain.StructuringRequest
import com.example.lifelog.structuring.domain.StructuringResult

interface EventStructurer {
    fun structure(request: StructuringRequest): StructuringResult
}
