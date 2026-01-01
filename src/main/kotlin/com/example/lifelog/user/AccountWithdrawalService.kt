package com.example.lifelog.user

import com.example.lifelog.auth.OAuthAccountRepository
import com.example.lifelog.auth.RefreshTokenRepository
import com.example.lifelog.auth.security.AuthContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccountWithdrawalService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val oauthAccountRepository: OAuthAccountRepository,
) {
    @Transactional
    fun withdrawMe() {
        val userId = AuthContext.currentUserId()
        val user =
            userRepository.findById(userId).orElseThrow {
                IllegalStateException("User not found: $userId")
            }

        if (user.deletedAt != null) return
        user.delete()

        refreshTokenRepository.deleteAllByUserId(userId)
        oauthAccountRepository.deleteAllByUserId(userId)
    }
}
