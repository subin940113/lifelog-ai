package com.example.lifelog.domain.auth

/**
 * 애플 refresh_token 도메인 리포지토리 인터페이스
 * 구현체는 infrastructure 레이어에 위치
 */
interface AppleRefreshTokenRepository {
    fun findByUserId(userId: Long): AppleRefreshToken?

    fun save(token: AppleRefreshToken): AppleRefreshToken

    fun deleteByUserId(userId: Long)
}
