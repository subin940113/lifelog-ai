package com.example.lifelog.auth

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class KakaoOAuthProviderImpl(
    private val webClient: WebClient,
) : KakaoOAuthProvider {
    override fun fetchProfile(accessToken: String): OAuthProfile {
        val res =
            webClient
                .get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .bodyToMono(Map::class.java)
                .block() ?: throw IllegalArgumentException("Kakao profile empty")

        val id = (res["id"] ?: "").toString().trim()
        if (id.isBlank()) throw IllegalArgumentException("Kakao providerUserId missing")

        // kakao_account.profile.nickname / email 등은 앱 권한 및 설정에 따라 null 가능
        val kakaoAccount = res["kakao_account"] as? Map<*, *>
        val profile = kakaoAccount?.get("profile") as? Map<*, *>

        val nickname = profile?.get("nickname")?.toString()
        val email = kakaoAccount?.get("email")?.toString()

        return OAuthProfile(
            providerUserId = id,
            email = email,
            displayName = nickname,
        )
    }
}
