package com.example.lifelog.push.fcm

interface FcmClient {
    fun send(
        token: String,
        title: String,
        body: String,
        data: Map<String, String>,
    )
}
