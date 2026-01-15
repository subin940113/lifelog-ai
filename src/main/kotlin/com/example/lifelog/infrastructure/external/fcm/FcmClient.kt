package com.example.lifelog.infrastructure.external.fcm

/**
 * FCM 클라이언트 인터페이스
 */
interface FcmClient {
    fun send(
        token: String,
        title: String,
        body: String,
        data: Map<String, String>,
    )
}
