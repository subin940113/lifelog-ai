package com.example.lifelog.auth

data class OAuthProfile(
    val providerUserId: String, // sub
    val email: String?,
    val displayName: String?,
)

interface GoogleOAuthProvider {
    fun verify(idToken: String): OAuthProfile
}

interface KakaoOAuthProvider {
    fun fetchProfile(accessToken: String): OAuthProfile
}

interface NaverOAuthProvider {
    fun fetchProfile(accessToken: String): OAuthProfile
}
