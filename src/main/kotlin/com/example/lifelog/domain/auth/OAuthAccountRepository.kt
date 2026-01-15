package com.example.lifelog.domain.auth

/**
 * OAuth 계정 도메인 리포지토리 인터페이스
 * 구현체는 infrastructure 레이어에 위치
 */
interface OAuthAccountRepository {
    fun findByProviderAndProviderUserId(
        provider: OAuthProvider,
        providerUserId: String,
    ): OAuthAccount?

    fun save(account: OAuthAccount): OAuthAccount

    fun deleteAllByUserId(userId: Long)
}
