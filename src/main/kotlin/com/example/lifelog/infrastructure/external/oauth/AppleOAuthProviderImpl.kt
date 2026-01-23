package com.example.lifelog.infrastructure.external.oauth

import com.example.lifelog.common.exception.BusinessException
import com.example.lifelog.common.exception.ErrorCode
import com.example.lifelog.common.exception.ValidationException
import com.example.lifelog.infrastructure.config.AppleOAuthProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.Jwts.SIG.ES256
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Duration
import java.time.Instant
import java.util.Base64
import java.util.Date

/**
 * Apple OAuth Provider 구현체
 */
@Component
class AppleOAuthProviderImpl(
    private val webClient: WebClient,
    private val properties: AppleOAuthProperties,
    private val objectMapper: ObjectMapper,
) : AppleOAuthProvider {
    private val log = LoggerFactory.getLogger(AppleOAuthProviderImpl::class.java)

    init {
        if (properties.clientId.isBlank() || properties.teamId.isBlank() || properties.keyId.isBlank() || properties.privateKey.isBlank()) {
            throw ValidationException(ErrorCode.VALIDATION_REQUIRED, "oauth.apple configuration must not be empty")
        }
    }

    override fun fetchProfile(authorizationCode: String): OAuthProfile {
        val code = authorizationCode.trim()
        if (code.isEmpty()) {
            throw ValidationException(ErrorCode.VALIDATION_REQUIRED, "authorizationCode is empty")
        }

        try {
            // 1. authorizationCode로 토큰 교환
            val tokenResponse = exchangeToken(code)
            val idToken =
                tokenResponse.idToken
                    ?: throw BusinessException(ErrorCode.BUSINESS_OAUTH_PROVIDER_ERROR, "Apple id_token missing")

            // 2. id_token에서 사용자 정보 추출
            return parseIdToken(idToken)
        } catch (e: WebClientResponseException.Unauthorized) {
            val responseBody = e.responseBodyAsString
            log.warn(
                "[OAUTH][APPLE] unauthorized (401). clientId={}, teamId={}, keyId={}, body={}",
                properties.clientId,
                properties.teamId,
                properties.keyId,
                responseBody,
                e,
            )
            throw BusinessException(ErrorCode.BUSINESS_OAUTH_UNAUTHORIZED, "Apple unauthorized (401). body=$responseBody", e)
        } catch (e: WebClientResponseException) {
            val responseBody = e.responseBodyAsString
            log.error(
                "[OAUTH][APPLE] token exchange failed. clientId={}, teamId={}, keyId={}, status={}, body={}",
                properties.clientId,
                properties.teamId,
                properties.keyId,
                e.statusCode.value(),
                responseBody,
                e,
            )
            throw BusinessException(ErrorCode.BUSINESS_OAUTH_PROVIDER_ERROR, "Apple token exchange failed. body=$responseBody", e)
        }
    }

    override fun fetchProfileAndRefreshToken(authorizationCode: String): AppleProfileWithToken {
        val code = authorizationCode.trim()
        if (code.isEmpty()) {
            throw ValidationException(ErrorCode.VALIDATION_REQUIRED, "authorizationCode is empty")
        }

        try {
            // 1. authorizationCode로 토큰 교환
            val tokenResponse = exchangeToken(code)
            val idToken =
                tokenResponse.idToken
                    ?: throw BusinessException(ErrorCode.BUSINESS_OAUTH_PROVIDER_ERROR, "Apple id_token missing")

            // 2. id_token에서 사용자 정보 추출
            val profile = parseIdToken(idToken)

            return AppleProfileWithToken(
                profile = profile,
                refreshToken = tokenResponse.refreshToken,
            )
        } catch (e: WebClientResponseException.Unauthorized) {
            val responseBody = e.responseBodyAsString
            log.warn(
                "[OAUTH][APPLE] unauthorized (401). clientId={}, teamId={}, keyId={}, body={}",
                properties.clientId,
                properties.teamId,
                properties.keyId,
                responseBody,
                e,
            )
            throw BusinessException(ErrorCode.BUSINESS_OAUTH_UNAUTHORIZED, "Apple unauthorized (401). body=$responseBody", e)
        } catch (e: WebClientResponseException) {
            val responseBody = e.responseBodyAsString
            log.error(
                "[OAUTH][APPLE] token exchange failed. clientId={}, teamId={}, keyId={}, status={}, body={}",
                properties.clientId,
                properties.teamId,
                properties.keyId,
                e.statusCode.value(),
                responseBody,
                e,
            )
            throw BusinessException(ErrorCode.BUSINESS_OAUTH_PROVIDER_ERROR, "Apple token exchange failed. body=$responseBody", e)
        }
    }

    private fun exchangeToken(authorizationCode: String): AppleTokenResponse {
        val clientSecret = generateClientSecret()

        val formData: MultiValueMap<String, String> = LinkedMultiValueMap()
        formData.add("client_id", properties.clientId)
        formData.add("client_secret", clientSecret)
        formData.add("code", authorizationCode)
        formData.add("grant_type", "authorization_code")

        // NOTE: 일부 구현(웹 기반 인증 플로우)은 redirect_uri가 반드시 필요하며, 누락 시 Apple이 invalid_client/invalid_grant를 반환할 수 있음.
        //      Properties에 필드를 추가하지 않고도 바로 적용 가능하도록 env로 받는다.
        val redirectUri = System.getenv("APPLE_REDIRECT_URI")?.trim().orEmpty()
        if (redirectUri.isNotBlank()) {
            formData.add("redirect_uri", redirectUri)
            log.info("[OAUTH][APPLE] using redirect_uri from env. redirectUri={}", redirectUri)
        }

        val response =
            webClient
                .post()
                .uri("https://appleid.apple.com/auth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(AppleTokenResponse::class.java)
                .timeout(Duration.ofSeconds(10))
                .block()
                ?: throw BusinessException(ErrorCode.BUSINESS_OAUTH_PROVIDER_ERROR, "Apple token exchange returned empty body")

        return response
    }

    private fun generateClientSecret(): String {
        val now = Instant.now()
        val expiration = now.plusSeconds(3600) // 1시간

        val privateKey = parsePrivateKey(properties.privateKey)

        return Jwts
            .builder()
            .header()
            .keyId(properties.keyId)
            .and()
            .setIssuer(properties.teamId)
            .setAudience("https://appleid.apple.com")
            .setSubject(properties.clientId)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiration))
            .signWith(privateKey, ES256)
            .compact()
    }

    private fun parsePrivateKey(privateKeyContent: String): PrivateKey {
        try {
            // env var로 주입될 때 "\n"(두 글자)로 들어오거나, 따옴표로 감싸져 들어오는 케이스를 모두 정규화
            val normalized =
                privateKeyContent
                    .trim()
                    .removeSurrounding("\"")
                    .replace("\\r", "")
                    .replace("\\n", "\n") // 문자열 리터럴 형태의 \n -> 실제 개행

            // PEM 헤더/푸터 제거 후 base64만 남김 (공백/개행 제거)
            val base64Body =
                normalized
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("\n", "")
                    .replace("\t", "")
                    .replace(" ", "")

            if (base64Body.isBlank()) {
                throw IllegalArgumentException("Apple private key body is empty")
            }

            val keyBytes = Base64.getDecoder().decode(base64Body)
            val keySpec = PKCS8EncodedKeySpec(keyBytes)
            val keyFactory = KeyFactory.getInstance("EC")
            return keyFactory.generatePrivate(keySpec)
        } catch (e: Exception) {
            log.error(
                "[OAUTH][APPLE] failed to parse private key. length={}, hasBegin={}, hasEnd={}",
                privateKeyContent.length,
                privateKeyContent.contains("BEGIN"),
                privateKeyContent.contains("END"),
                e,
            )
            throw ValidationException(
                ErrorCode.VALIDATION_REQUIRED,
                "Invalid Apple private key format",
                e,
            )
        }
    }

    private fun parseIdToken(idToken: String): OAuthProfile {
        try {
            // JWT는 header.payload.signature 형태
            val parts = idToken.split(".")
            if (parts.size != 3) {
                throw BusinessException(ErrorCode.BUSINESS_OAUTH_PROVIDER_ERROR, "Invalid Apple id_token format")
            }

            // payload 디코딩 (Base64 URL-safe)
            val payload = String(Base64.getUrlDecoder().decode(parts[1]))
            val claims = objectMapper.readValue(payload, Map::class.java) as Map<*, *>

            val sub =
                claims["sub"]?.toString()
                    ?: throw BusinessException(ErrorCode.BUSINESS_OAUTH_PROVIDER_ERROR, "Apple sub missing in id_token")
            val email = claims["email"]?.toString()
            val emailVerified = claims["email_verified"] as? Boolean ?: false

            // email이 없거나 verified가 false일 수 있음 (애플의 경우)
            return OAuthProfile(
                providerUserId = sub,
                email = if (emailVerified) email else null,
                displayName = email, // 애플은 displayName을 제공하지 않으므로 email 사용
            )
        } catch (e: Exception) {
            throw BusinessException(ErrorCode.BUSINESS_OAUTH_PROVIDER_ERROR, "Failed to parse Apple id_token", e)
        }
    }

    override fun revokeToken(
        token: String,
        tokenTypeHint: String,
    ) {
        val trimmedToken = token.trim()
        if (trimmedToken.isEmpty()) {
            log.warn("[OAUTH][APPLE] revokeToken called with empty token")
            return
        }

        try {
            val clientSecret = generateClientSecret()

            val formData: MultiValueMap<String, String> = LinkedMultiValueMap()
            formData.add("client_id", properties.clientId)
            formData.add("client_secret", clientSecret)
            formData.add("token", trimmedToken)
            formData.add("token_type_hint", tokenTypeHint)

            webClient
                .post()
                .uri("https://appleid.apple.com/auth/revoke")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(String::class.java)
                .timeout(Duration.ofSeconds(10))
                .block()

            log.info("[OAUTH][APPLE] token revoked successfully. tokenTypeHint={}", tokenTypeHint)
        } catch (e: WebClientResponseException) {
            val responseBody = e.responseBodyAsString
            // 이미 revoke된 토큰이거나 유효하지 않은 토큰일 수 있음 (정상적인 경우)
            log.warn(
                "[OAUTH][APPLE] revokeToken failed. status={}, body={}, tokenTypeHint={}",
                e.statusCode.value(),
                responseBody,
                tokenTypeHint,
                e,
            )
            // 계정 삭제는 계속 진행되어야 하므로 예외를 던지지 않음
        } catch (e: Exception) {
            log.error(
                "[OAUTH][APPLE] revokeToken failed unexpectedly. tokenTypeHint={}",
                tokenTypeHint,
                e,
            )
            // 계정 삭제는 계속 진행되어야 하므로 예외를 던지지 않음
        }
    }
}

data class AppleTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String? = null,
    @JsonProperty("token_type")
    val tokenType: String? = null,
    @JsonProperty("expires_in")
    val expiresIn: Int? = null,
    @JsonProperty("refresh_token")
    val refreshToken: String? = null,
    @JsonProperty("id_token")
    val idToken: String? = null,
)
