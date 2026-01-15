package com.example.lifelog.infrastructure.security

import java.security.MessageDigest

/**
 * 토큰 해시 유틸리티
 */
object TokenHash {
    fun sha256Hex(raw: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(raw.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
