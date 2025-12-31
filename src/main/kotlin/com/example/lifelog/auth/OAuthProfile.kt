package com.example.lifelog.auth

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import org.springframework.stereotype.Component

data class OAuthProfile(
    val providerUserId: String, // sub
    val email: String?,
    val name: String?,
    val avatarUrl: String?,
    val emailVerified: Boolean,
)

@Component
class GoogleOAuthProvider(
    private val props: GoogleOAuthProperties,
) {
    private val verifier =
        GoogleIdTokenVerifier
            .Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
            ).setAudience(props.clientIds)
            .build()

    init {
        require(props.clientIds.isNotEmpty()) { "oauth.google.client-ids must not be empty" }
    }

    fun verify(idToken: String): OAuthProfile {
        val token =
            verifier.verify(idToken)
                ?: throw IllegalArgumentException("Invalid Google ID token")

        val p = token.payload
        return OAuthProfile(
            providerUserId = p.subject,
            email = p.email,
            name = p["name"] as String?,
            avatarUrl = p["picture"] as String?,
            emailVerified = p.emailVerified ?: false,
        )
    }
}
