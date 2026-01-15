package com.example.lifelog.domain.auth

/**
 * 리프레시 토큰 도메인 리포지토리 인터페이스
 * 구현체는 infrastructure 레이어에 위치
 */
interface RefreshTokenRepository {
    fun findByTokenHash(tokenHash: String): RefreshToken?

    fun save(token: RefreshToken): RefreshToken

    fun deleteAllByUserId(userId: Long)
}
