package com.example.lifelog.user

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    @Transactional(readOnly = true)
    fun getMe(userId: Long): UserMeResponse {
        val user =
            userRepository.findById(userId).orElseThrow {
                IllegalStateException("User not found: $userId")
            }

        return UserMeResponse(
            id = user.id,
            displayName = user.displayName,
            createdAt = user.createdAt,
            lastLoginAt = user.lastLoginAt,
        )
    }

    @Transactional
    fun updateMe(
        userId: Long,
        req: UpdateUserMeRequest,
    ): UserMeResponse {
        val user =
            userRepository.findById(userId).orElseThrow {
                IllegalStateException("User not found: $userId")
            }

        if (!req.displayName.isNullOrBlank()) {
            user.updateDisplayName(req.displayName.trim())
        }

        return UserMeResponse(
            id = user.id,
            displayName = user.displayName,
            createdAt = user.createdAt,
            lastLoginAt = user.lastLoginAt,
        )
    }
}
