package com.example.lifelog.auth

import com.example.lifelog.user.User
import com.example.lifelog.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val googleOAuthProvider: GoogleOAuthProvider,
    private val kakaoOAuthProvider: KakaoOAuthProvider,
    private val naverOAuthProvider: NaverOAuthProvider,
    private val jwtProvider: JwtProvider,
    private val userRepository: UserRepository,
    private val oauthAccountRepository: OAuthAccountRepository,
    private val refreshTokenService: RefreshTokenService,
) {
    @Transactional
    fun loginGoogle(idToken: String): AuthLoginResult {
        val profile = googleOAuthProvider.verify(idToken)

        // Google providerUserId + displayName(기존 코드에서는 email 사용)
        return loginOrSignUp(
            provider = OAuthProvider.GOOGLE,
            providerUserId = profile.providerUserId,
            displayNameCandidate = profile.email, // 기존 유지
        )
    }

    @Transactional
    fun loginKakao(accessToken: String): AuthLoginResult {
        val profile = kakaoOAuthProvider.fetchProfile(accessToken)

        return loginOrSignUp(
            provider = OAuthProvider.KAKAO,
            providerUserId = profile.providerUserId,
            displayNameCandidate = profile.email,
        )
    }

    @Transactional
    fun loginNaver(accessToken: String): AuthLoginResult {
        val profile = naverOAuthProvider.fetchProfile(accessToken)

        return loginOrSignUp(
            provider = OAuthProvider.NAVER,
            providerUserId = profile.providerUserId,
            displayNameCandidate = profile.email,
        )
    }

    private fun loginOrSignUp(
        provider: OAuthProvider,
        providerUserId: String,
        displayNameCandidate: String?,
    ): AuthLoginResult {
        val existingAccount =
            oauthAccountRepository.findByProviderAndProviderUserId(provider, providerUserId)

        if (existingAccount != null) {
            val user = userRepository.findById(existingAccount.userId).orElseThrow()
            user.updateLastLoginAt()
            userRepository.save(user)

            return AuthLoginResult(
                accessToken = jwtProvider.createAccessToken(existingAccount.userId),
                refreshToken = refreshTokenService.issueForUser(existingAccount.userId),
                displayName = user.displayName,
                isNewUser = false,
            )
        }

        // 신규 유저 생성: displayName이 비면 fallback
        val safeName = (displayNameCandidate ?: "").trim().ifBlank { "계정" }
        val newUser = userRepository.save(User(displayName = safeName))

        oauthAccountRepository.save(
            OAuthAccount(
                provider = provider,
                providerUserId = providerUserId,
                userId = newUser.id,
            ),
        )

        return AuthLoginResult(
            accessToken = jwtProvider.createAccessToken(newUser.id),
            refreshToken = refreshTokenService.issueForUser(newUser.id),
            displayName = newUser.displayName,
            isNewUser = true,
        )
    }

    fun refresh(req: TokenRefreshRequest): TokenRefreshResponse {
        val rotation = refreshTokenService.rotate(req.refreshToken)
        val newAccess = jwtProvider.createAccessToken(rotation.userId)
        return TokenRefreshResponse(
            accessToken = newAccess,
            refreshToken = rotation.newRefreshToken,
        )
    }

    @Transactional
    fun logout(
        refreshToken: String,
        allDevices: Boolean = false,
    ) {
        if (allDevices) {
            val userId = refreshTokenService.requireValid(refreshToken).userId
            refreshTokenService.revokeAllForUser(userId)
        } else {
            refreshTokenService.revoke(refreshToken)
        }
    }
}
