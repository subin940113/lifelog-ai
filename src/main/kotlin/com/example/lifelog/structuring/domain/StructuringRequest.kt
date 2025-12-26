package com.example.lifelog.structuring.domain

import java.time.Instant

data class StructuringRequest(
    val rawLogId: Long,
    val userId: Long?,
    val content: String,
    val createdAt: Instant,
    val timezone: String = "Asia/Seoul",
    val options: StructuringOptions = StructuringOptions(),
)

data class StructuringOptions(
    val allowMultipleEvents: Boolean = false, // MVP: false (단일 이벤트만)
    val minConfidence: Double = 0.0,
    val modelVersion: String? = null, // 추후 프롬프트/모델 버전 트래킹
)
