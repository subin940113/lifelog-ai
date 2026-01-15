package com.example.lifelog.application.push

import com.example.lifelog.domain.push.PushTokenRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 푸시 토큰 삭제 Use Case
 */
@Service
class DeletePushTokenUseCase(
    private val pushTokenRepository: PushTokenRepository,
) {
    @Transactional
    fun execute(
        userId: Long,
        token: String,
    ): Boolean {
        val deleted = pushTokenRepository.deleteByUserIdAndToken(userId, token.trim())
        return deleted > 0
    }
}
