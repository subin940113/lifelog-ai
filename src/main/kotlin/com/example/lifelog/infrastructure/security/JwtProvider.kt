package com.example.lifelog.infrastructure.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date

/**
 * JWT 토큰 생성 및 파싱 Provider
 */
@Component
class JwtProvider(
    @Value("\${jwt.secret}") secret: String,
    @Value("\${jwt.access-token-minutes:15}") private val accessMinutes: Long,
) {
    private val key = Keys.hmacShaKeyFor(secret.toByteArray())

    fun createAccessToken(userId: Long): String {
        val now = System.currentTimeMillis()
        val exp = now + accessMinutes * 60_000
        return Jwts
            .builder()
            .subject(userId.toString())
            .issuedAt(Date(now))
            .expiration(Date(exp))
            .signWith(key)
            .compact()
    }

    fun parseUserId(token: String): Long {
        val claims =
            Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
        return claims.subject.toLong()
    }
}
