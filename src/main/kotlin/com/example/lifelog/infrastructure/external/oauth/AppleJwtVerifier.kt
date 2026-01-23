package com.example.lifelog.infrastructure.external.oauth

import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.security.cert.X509Certificate
import java.util.Base64

/**
 * 애플 JWT 검증 컴포넌트
 * 애플의 서버-to-서버 알림 JWT를 검증하고 파싱
 */
@Component
class AppleJwtVerifier(
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * JWT 검증 및 파싱
     * 애플의 서버-to-서버 알림은 OIDC JWS 키로 서명됨
     */
    fun verifyAndParse(jwtPayload: String): Claims {
        try {
            // JWT 헤더에서 x5c (인증서 체인) 추출
            val parts = jwtPayload.split(".")
            if (parts.size != 3) {
                throw IllegalArgumentException("Invalid JWT format")
            }

            val headerJson = String(Base64.getUrlDecoder().decode(parts[0]))
            val header = objectMapper.readValue(headerJson, Map::class.java) as Map<*, *>
            val x5c =
                (header["x5c"] as? List<*>)?.firstOrNull() as? String
                    ?: throw IllegalArgumentException("x5c not found in JWT header")

            // 인증서 파싱
            val certBytes = Base64.getDecoder().decode(x5c)
            val certFactory =
                java.security.cert.CertificateFactory
                    .getInstance("X.509")
            val cert = certFactory.generateCertificate(java.io.ByteArrayInputStream(certBytes)) as X509Certificate

            // JWT 검증 및 파싱
            val publicKey = cert.publicKey
            val claims =
                Jwts
                    .parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(jwtPayload)
                    .payload

            // 발급자 확인
            val iss = claims.issuer
            if (iss != "https://appleid.apple.com") {
                throw IllegalArgumentException("Invalid issuer: $iss")
            }

            return claims
        } catch (e: Exception) {
            log.error("[APPLE_JWT] JWT verification failed", e)
            throw RuntimeException("Failed to verify JWT", e)
        }
    }
}
