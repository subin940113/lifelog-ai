package com.example.lifelog.auth

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.Duration

@Component
class NaverOAuthProviderImpl(
    private val webClient: WebClient,
) : NaverOAuthProvider {
    override fun fetchProfile(naverAccessToken: String): OAuthProfile {
        val token = naverAccessToken.trim()
        require(token.isNotEmpty()) { "naverAccessToken is empty" }

        try {
            val res =
                webClient
                    .get()
                    .uri("https://openapi.naver.com/v1/nid/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(NaverMeResponse::class.java)
                    .timeout(Duration.ofSeconds(8)) // 네이버 응답이 지연되면 여기서 끊김
                    .block() ?: throw IllegalStateException("Naver /me returned empty body")

            // 네이버 /me 응답은 resultcode / message / response 구조
            val body = res.response ?: throw IllegalArgumentException("Naver response is null: ${res.message}")

            return OAuthProfile(
                providerUserId = body.id ?: throw IllegalArgumentException("Naver id missing"),
                email = body.email,
                displayName = body.name ?: body.nickname ?: body.email,
            )
        } catch (e: WebClientResponseException.Unauthorized) {
            // ★ 여기가 핵심: 네이버가 준 바디를 반드시 로그로 남겨야 원인을 확정 가능
            // (토큰 만료/잘못된 토큰/권한 부족 등)
            val responseBody = e.responseBodyAsString
            throw IllegalArgumentException("Naver unauthorized (401). body=$responseBody", e)
        }
    }
}

data class NaverMeResponse(
    val resultcode: String? = null,
    val message: String? = null,
    val response: NaverMeBody? = null,
)

data class NaverMeBody(
    val id: String? = null,
    val email: String? = null,
    val name: String? = null,
    val nickname: String? = null,
)
