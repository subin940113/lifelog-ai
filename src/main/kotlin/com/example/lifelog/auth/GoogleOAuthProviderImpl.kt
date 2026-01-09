package com.example.lifelog.auth

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import org.springframework.stereotype.Component

@Component
class GoogleOAuthProviderImpl(
    private val props: GoogleOAuthProperties,
) : GoogleOAuthProvider {
    private val verifier: GoogleIdTokenVerifier =
        GoogleIdTokenVerifier
            .Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
            ).setAudience(props.clientIds)
            .build()

    init {
        require(props.clientIds.isNotEmpty()) { "oauth.google.client-ids must not be empty" }
    }

    override fun verify(idToken: String): OAuthProfile {
        val token =
            verifier.verify(idToken)
                ?: throw IllegalArgumentException("Invalid Google ID token")

        val p = token.payload

        return OAuthProfile(
            providerUserId = p.subject,
            email = p.email,
            displayName = (p["name"] as? String) ?: p.email,
        )
    }
}
