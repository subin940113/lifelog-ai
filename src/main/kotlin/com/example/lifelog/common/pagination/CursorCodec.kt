package com.example.lifelog.common.pagination

import java.time.Instant
import java.util.Base64

/**
 * 커서 기반 페이지네이션을 위한 인코더/디코더
 * cursor payload: "<epochMillis>:<id>"
 */
object CursorCodec {
    fun encode(
        createdAt: Instant,
        id: Long,
    ): String {
        val payload = "${createdAt.toEpochMilli()}:$id"
        return Base64.getUrlEncoder().withoutPadding().encodeToString(payload.toByteArray(Charsets.UTF_8))
    }

    fun decode(cursor: String?): Pair<Instant?, Long?> {
        if (cursor.isNullOrBlank()) return Pair(null, null)

        return try {
            val decoded = String(Base64.getUrlDecoder().decode(cursor), Charsets.UTF_8)
            val parts = decoded.split(":")
            if (parts.size != 2) return Pair(null, null)

            val ms = parts[0].toLongOrNull() ?: return Pair(null, null)
            val id = parts[1].toLongOrNull() ?: return Pair(null, null)

            Pair(Instant.ofEpochMilli(ms), id)
        } catch (_: Exception) {
            Pair(null, null)
        }
    }
}
