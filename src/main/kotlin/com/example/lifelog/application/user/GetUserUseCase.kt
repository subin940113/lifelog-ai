package com.example.lifelog.application.user

import com.example.lifelog.domain.user.User
import com.example.lifelog.domain.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 사용자 정보 조회 Use Case
 */
@Service
class GetUserUseCase(
    private val userRepository: UserRepository,
) {
    @Transactional(readOnly = true)
    fun execute(userId: Long): UserResponse {
        val user =
            userRepository.findById(userId)
                ?: throw IllegalStateException("User not found: $userId")

        return UserResponse.from(user)
    }
}

/**
 * 사용자 정보 업데이트 Use Case
 */
@Service
class UpdateUserUseCase(
    private val userRepository: UserRepository,
) {
    @Transactional
    fun execute(
        userId: Long,
        request: UpdateUserRequest,
    ): UserResponse {
        val user =
            userRepository.findById(userId)
                ?: throw IllegalStateException("User not found: $userId")

        if (!request.displayName.isNullOrBlank()) {
            user.updateDisplayName(request.displayName.trim())
            userRepository.save(user)
        }

        return UserResponse.from(user)
    }
}

/**
 * 사용자 응답 DTO
 */
data class UserResponse(
    val id: Long,
    val displayName: String,
    val createdAt: java.time.Instant,
    val lastLoginAt: java.time.Instant,
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id,
                displayName = user.displayName,
                createdAt = user.createdAt,
                lastLoginAt = user.lastLoginAt,
            )
        }
    }
}

/**
 * 사용자 업데이트 요청 DTO
 */
data class UpdateUserRequest(
    val displayName: String? = null,
)
