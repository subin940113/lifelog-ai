package com.example.lifelog.auth

import java.security.MessageDigest

object TokenHash {
    fun sha256Hex(raw: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(raw.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
