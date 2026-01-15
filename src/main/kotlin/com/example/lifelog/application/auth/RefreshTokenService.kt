package com.example.lifelog.application.auth

import com.example.lifelog.infrastructure.security.TokenHash
import com.example.lifelog.domain.auth.RefreshToken
import com.example.lifelog.domain.auth.RefreshTokenRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.Base64

/**
 * 리프레시 토큰 관리 Use Case
 */
@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    private val random = SecureRandom()

    // 운영에선 14~30일 권장
    private val refreshTtl: Duration = Duration.ofDays(14)

    fun generateRawToken(): String {
        val bytes = ByteArray(48) // 충분히 긴 랜덤
        random.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    @Transactional
    fun issueForUser(
        userId: Long,
        now: Instant = Instant.now(),
    ): String {
        val raw = generateRawToken()
        val hash = TokenHash.sha256Hex(raw)

        val entity =
            RefreshToken(
                userId = userId,
                tokenHash = hash,
                issuedAt = now,
                expiresAt = now.plus(refreshTtl),
            )
        refreshTokenRepository.save(entity)
        return raw
    }

    data class ValidRefreshToken(
        val userId: Long,
        val tokenHash: String,
        val expiresAt: Instant,
        val revokedAt: Instant?,
    )

    fun requireValid(
        rawRefreshToken: String,
        now: Instant = Instant.now(),
    ): ValidRefreshToken {
        val hash = TokenHash.sha256Hex(rawRefreshToken)
        val rt = refreshTokenRepository.findByTokenHash(hash)
            ?: throw UnauthorizedException("refresh token not found")

        if (rt.isRevoked()) throw UnauthorizedException("refresh token revoked")
        if (rt.isExpired(now)) throw UnauthorizedException("refresh token expired")

        return ValidRefreshToken(
            userId = rt.userId,
            tokenHash = rt.tokenHash,
            expiresAt = rt.expiresAt,
            revokedAt = rt.revokedAt,
        )
    }

    /**
     * Refresh Token 회전(rotate):
     * - 기존 refresh token 유효성 검증
     * - 새 refresh 발급 + DB 저장
     * - 기존 토큰 revoked + replaced_by_hash 설정
     * - 새 raw refresh token 반환
     */
    @Transactional
    fun rotate(
        rawRefreshToken: String,
        now: Instant = Instant.now(),
    ): RefreshRotationResult {
        val oldHash = TokenHash.sha256Hex(rawRefreshToken)
        val old = refreshTokenRepository.findByTokenHash(oldHash)
            ?: throw UnauthorizedException("refresh token not found")

        if (old.isRevoked()) throw UnauthorizedException("refresh token revoked")
        if (old.isExpired(now)) throw UnauthorizedException("refresh token expired")

        val newRaw = generateRawToken()
        val newHash = TokenHash.sha256Hex(newRaw)

        refreshTokenRepository.save(
            RefreshToken(
                userId = old.userId,
                tokenHash = newHash,
                issuedAt = now,
                expiresAt = now.plus(refreshTtl),
            ),
        )

        old.revokedAt = now
        old.replacedByHash = newHash
        refreshTokenRepository.save(old) // 변경사항 저장

        return RefreshRotationResult(
            userId = old.userId,
            newRefreshToken = newRaw,
        )
    }

    @Transactional
    fun revoke(
        rawRefreshToken: String,
        now: Instant = Instant.now(),
    ) {
        val hash = TokenHash.sha256Hex(rawRefreshToken)
        val rt = refreshTokenRepository.findByTokenHash(hash) ?: return

        if (rt.isRevoked()) return

        rt.revokedAt = now
        refreshTokenRepository.save(rt)
    }

    @Transactional
    fun revokeAllForUser(userId: Long) {
        refreshTokenRepository.deleteAllByUserId(userId)
    }
}

data class RefreshRotationResult(
    val userId: Long,
    val newRefreshToken: String,
)

class UnauthorizedException(
    message: String,
) : RuntimeException(message)
