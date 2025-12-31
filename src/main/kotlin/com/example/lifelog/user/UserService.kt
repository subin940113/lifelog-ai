package com.example.lifelog.user

import com.example.lifelog.auth.security.AuthContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    @Transactional(readOnly = true)
    fun getMe(): UserMeResponse {
        val userId = AuthContext.currentUserId()
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
    fun updateMe(req: UpdateUserMeRequest): UserMeResponse {
        val userId = AuthContext.currentUserId()
        val user =
            userRepository.findById(userId).orElseThrow {
                IllegalStateException("User not found: $userId")
            }

        if (!req.displayName.isNullOrBlank()) {
            user.displayName = req.displayName.trim()
        }

        return UserMeResponse(
            id = user.id,
            displayName = user.displayName,
            createdAt = user.createdAt,
            lastLoginAt = user.lastLoginAt,
        )
    }
}
