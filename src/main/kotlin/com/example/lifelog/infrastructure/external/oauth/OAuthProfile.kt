package com.example.lifelog.infrastructure.external.oauth

/**
 * OAuth 프로필 정보
 */
data class OAuthProfile(
    val providerUserId: String, // sub
    val email: String?,
    val displayName: String?,
)

/**
 * Google OAuth Provider 인터페이스
 */
interface GoogleOAuthProvider {
    fun verify(idToken: String): OAuthProfile
}

/**
 * Kakao OAuth Provider 인터페이스
 */
interface KakaoOAuthProvider {
    fun fetchProfile(accessToken: String): OAuthProfile
}

/**
 * Naver OAuth Provider 인터페이스
 */
interface NaverOAuthProvider {
    fun fetchProfile(accessToken: String): OAuthProfile
}

/**
 * Apple OAuth Provider 인터페이스
 */
interface AppleOAuthProvider {
    fun fetchProfile(authorizationCode: String): OAuthProfile
}
