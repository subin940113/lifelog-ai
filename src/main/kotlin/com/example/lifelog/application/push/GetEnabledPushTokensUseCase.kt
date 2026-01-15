package com.example.lifelog.application.push

import com.example.lifelog.domain.push.PushTokenRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 활성화된 푸시 토큰 조회 Use Case
 */
@Service
class GetEnabledPushTokensUseCase(
    private val pushTokenRepository: PushTokenRepository,
) {
    @Transactional(readOnly = true)
    fun execute(userId: Long): List<String> = pushTokenRepository.findEnabledByUserId(userId).map { it.token }
}
