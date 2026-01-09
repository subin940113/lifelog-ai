package com.example.lifelog.user

import com.example.lifelog.auth.security.AuthContext
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val em: EntityManager,
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
            user.updateDisplayName(req.displayName.trim())
        }

        return UserMeResponse(
            id = user.id,
            displayName = user.displayName,
            createdAt = user.createdAt,
            lastLoginAt = user.lastLoginAt,
        )
    }

    @Transactional
    fun deleteMe(userId: Long) {
        val user =
            userRepository.findById(userId).orElseThrow {
                IllegalStateException("User not found: $userId")
            }

        if (user.deletedAt != null) return

        user.delete()
    }
}
