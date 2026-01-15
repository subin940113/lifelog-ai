package com.example.lifelog.domain.push

/**
 * 푸시 토큰 도메인 리포지토리 인터페이스
 * 구현체는 infrastructure 레이어에 위치
 */
interface PushTokenRepository {
    fun findByUserIdAndToken(
        userId: Long,
        token: String,
    ): PushToken?

    fun findByUserIdAndEnabledTrue(userId: Long): List<PushToken>

    fun findEnabledByUserId(userId: Long): List<PushToken>

    fun save(token: PushToken): PushToken

    fun saveAll(tokens: List<PushToken>): List<PushToken>

    fun deleteByUserIdAndToken(
        userId: Long,
        token: String,
    ): Long

    fun findDistinctEnabledUserIds(): List<Long>

    fun findAllByUserIdAndEnabledTrue(userId: Long): List<PushToken>

    fun deleteByToken(token: String)
}
