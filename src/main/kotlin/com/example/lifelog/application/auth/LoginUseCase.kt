package com.example.lifelog.application.auth

import com.example.lifelog.common.NicknameGenerator
import com.example.lifelog.common.exception.ErrorCode
import com.example.lifelog.common.exception.NotFoundException
import com.example.lifelog.domain.auth.AppleRefreshToken
import com.example.lifelog.domain.auth.AppleRefreshTokenRepository
import com.example.lifelog.domain.auth.OAuthAccount
import com.example.lifelog.domain.auth.OAuthAccountRepository
import com.example.lifelog.domain.auth.OAuthProvider
import com.example.lifelog.domain.user.User
import com.example.lifelog.domain.user.UserRepository
import com.example.lifelog.infrastructure.external.oauth.AppleOAuthProvider
import com.example.lifelog.infrastructure.external.oauth.GoogleOAuthProvider
import com.example.lifelog.infrastructure.external.oauth.KakaoOAuthProvider
import com.example.lifelog.infrastructure.external.oauth.NaverOAuthProvider
import com.example.lifelog.infrastructure.security.JwtProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * OAuth 로그인 Use Case
 */
@Service
class LoginUseCase(
    private val googleOAuthProvider: GoogleOAuthProvider,
    private val kakaoOAuthProvider: KakaoOAuthProvider,
    private val naverOAuthProvider: NaverOAuthProvider,
    private val appleOAuthProvider: AppleOAuthProvider,
    private val jwtProvider: JwtProvider,
    private val userRepository: UserRepository,
    private val oauthAccountRepository: OAuthAccountRepository,
    private val appleRefreshTokenRepository: AppleRefreshTokenRepository,
    private val refreshTokenManagementUseCase: RefreshTokenManagementUseCase,
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

    @Transactional
    fun loginApple(authorizationCode: String): AuthLoginResult {
        val profileWithToken = appleOAuthProvider.fetchProfileAndRefreshToken(authorizationCode)
        val profile = profileWithToken.profile

        return loginOrSignUp(
            provider = OAuthProvider.APPLE,
            providerUserId = profile.providerUserId,
            displayNameCandidate = profile.email,
            appleRefreshToken = profileWithToken.refreshToken,
        )
    }

    private fun loginOrSignUp(
        provider: OAuthProvider,
        providerUserId: String,
        displayNameCandidate: String?,
        appleRefreshToken: String? = null,
    ): AuthLoginResult {
        val existingAccount =
            oauthAccountRepository.findByProviderAndProviderUserId(provider, providerUserId)

        if (existingAccount != null) {
            val user =
                userRepository.findById(existingAccount.userId)
                    ?: throw NotFoundException(ErrorCode.NOT_FOUND_USER, "User not found: ${existingAccount.userId}")
            user.updateLastLoginAt()
            userRepository.save(user)

            // 애플 계정인 경우 refresh_token 저장/업데이트
            if (provider == OAuthProvider.APPLE && appleRefreshToken != null) {
                val now = Instant.now()
                val existingToken = appleRefreshTokenRepository.findByUserId(existingAccount.userId)
                if (existingToken != null) {
                    existingToken.refreshToken = appleRefreshToken
                    existingToken.updatedAt = now
                    appleRefreshTokenRepository.save(existingToken)
                } else {
                    appleRefreshTokenRepository.save(
                        AppleRefreshToken(
                            userId = existingAccount.userId,
                            refreshToken = appleRefreshToken,
                            createdAt = now,
                            updatedAt = now,
                        ),
                    )
                }
            }

            return AuthLoginResult(
                accessToken = jwtProvider.createAccessToken(existingAccount.userId),
                refreshToken = refreshTokenManagementUseCase.issueForUser(existingAccount.userId),
                displayName = user.displayName,
                isNewUser = false,
            )
        }

        // 신규 유저 생성
        val safeName = NicknameGenerator.generateRandomNickname()
        val newUser = userRepository.save(User(displayName = safeName))

        oauthAccountRepository.save(
            OAuthAccount(
                provider = provider,
                providerUserId = providerUserId,
                userId = newUser.id,
            ),
        )

        // 애플 계정인 경우 refresh_token 저장
        if (provider == OAuthProvider.APPLE && appleRefreshToken != null) {
            val now = Instant.now()
            appleRefreshTokenRepository.save(
                AppleRefreshToken(
                    userId = newUser.id,
                    refreshToken = appleRefreshToken,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        }

        return AuthLoginResult(
            accessToken = jwtProvider.createAccessToken(newUser.id),
            refreshToken = refreshTokenManagementUseCase.issueForUser(newUser.id),
            displayName = newUser.displayName,
            isNewUser = true,
        )
    }
}

/**
 * 인증 로그인 결과
 */
data class AuthLoginResult(
    val accessToken: String,
    val refreshToken: String,
    val displayName: String,
    val isNewUser: Boolean,
)
