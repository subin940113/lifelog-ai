package com.example.lifelog.application.user

import com.example.lifelog.common.exception.ErrorCode
import com.example.lifelog.common.exception.NotFoundException
import com.example.lifelog.domain.auth.OAuthAccountRepository
import com.example.lifelog.domain.auth.RefreshTokenRepository
import com.example.lifelog.domain.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 사용자 계정 탈퇴 Use Case
 */
@Service
class WithdrawUserUseCase(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val oauthAccountRepository: OAuthAccountRepository,
) {
    @Transactional
    fun execute(userId: Long) {
        val user =
            userRepository.findById(userId)
                ?: throw NotFoundException(ErrorCode.NOT_FOUND_USER, "User not found: $userId")

        if (user.isDeleted()) return

        user.delete()
        userRepository.save(user)

        // 관련 데이터 삭제
        refreshTokenRepository.deleteAllByUserId(userId)
        oauthAccountRepository.deleteAllByUserId(userId)
    }
}
