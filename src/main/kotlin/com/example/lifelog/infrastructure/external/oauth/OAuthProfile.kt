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

    /**
     * 애플 프로필 및 refresh_token 조회
     * refresh_token은 계정 삭제 시 revoke에 사용됨
     */
    fun fetchProfileAndRefreshToken(authorizationCode: String): AppleProfileWithToken

    /**
     * 애플 토큰 revoke (계정 삭제 시 호출)
     * @param refreshToken 또는 accessToken
     * @param tokenTypeHint "refresh_token" 또는 "access_token"
     */
    fun revokeToken(
        token: String,
        tokenTypeHint: String = "refresh_token",
    )
}

/**
 * 애플 프로필 및 refresh_token
 */
data class AppleProfileWithToken(
    val profile: OAuthProfile,
    val refreshToken: String?,
)
