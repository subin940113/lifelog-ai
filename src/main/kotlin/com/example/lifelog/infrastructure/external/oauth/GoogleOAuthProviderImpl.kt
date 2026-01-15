package com.example.lifelog.infrastructure.external.oauth

import com.example.lifelog.common.exception.BusinessException
import com.example.lifelog.common.exception.ErrorCode
import com.example.lifelog.common.exception.ValidationException
import com.example.lifelog.infrastructure.config.GoogleOAuthProperties
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import org.springframework.stereotype.Component

/**
 * Google OAuth Provider 구현체
 */
@Component
class GoogleOAuthProviderImpl(
    private val properties: GoogleOAuthProperties,
) : GoogleOAuthProvider {
    private val verifier: GoogleIdTokenVerifier =
        GoogleIdTokenVerifier
            .Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
            ).setAudience(properties.clientIds)
            .build()

    init {
        if (properties.clientIds.isEmpty()) {
            throw ValidationException(ErrorCode.VALIDATION_REQUIRED, "oauth.google.client-ids must not be empty")
        }
    }

    override fun verify(idToken: String): OAuthProfile {
        val token =
            verifier.verify(idToken)
                ?: throw BusinessException(ErrorCode.BUSINESS_OAUTH_UNAUTHORIZED, "Invalid Google ID token")

        val payload = token.payload

        return OAuthProfile(
            providerUserId = payload.subject,
            email = payload.email,
            displayName = (payload["name"] as? String) ?: payload.email,
        )
    }
}
