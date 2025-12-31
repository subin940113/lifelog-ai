package com.example.lifelog.auth

import com.example.lifelog.user.User
import com.example.lifelog.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val googleOAuthProvider: GoogleOAuthProvider,
    private val jwtProvider: JwtProvider,
    private val userRepository: UserRepository,
    private val oauthAccountRepository: OAuthAccountRepository,
) {

    @Transactional
    fun loginGoogle(idToken: String): GoogleLoginResult {
        val profile = googleOAuthProvider.verify(idToken)

        if (!profile.emailVerified) {
            throw IllegalArgumentException("Google email not verified")
        }

        val providerUserId = profile.providerUserId

        // 1️⃣ 이미 연동된 OAuth 계정이 있는지 확인
        val existingAccount =
            oauthAccountRepository.findByProviderAndProviderUserId(
                OAuthProvider.GOOGLE,
                providerUserId,
            )

        if (existingAccount != null) {
            return GoogleLoginResult(
                accessToken = jwtProvider.createAccessToken(existingAccount.userId),
                isNewUser = false,
            )
        }

        // 2️⃣ 없으면 → 회원가입
        val newUser = userRepository.save(User())

        oauthAccountRepository.save(
            OAuthAccount(
                provider = OAuthProvider.GOOGLE,
                providerUserId = providerUserId,
                userId = newUser.id,
                email = profile.email,
                displayName = profile.name,
                pictureUrl = profile.avatarUrl,
            )
        )

        return GoogleLoginResult(
            accessToken = jwtProvider.createAccessToken(newUser.id),
            isNewUser = true,
        )
    }
}