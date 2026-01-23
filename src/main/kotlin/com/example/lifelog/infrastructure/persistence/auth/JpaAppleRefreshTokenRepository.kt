package com.example.lifelog.infrastructure.persistence.auth

import com.example.lifelog.domain.auth.AppleRefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaAppleRefreshTokenRepository : JpaRepository<AppleRefreshToken, Long> {
    fun findByUserId(userId: Long): AppleRefreshToken?

    fun deleteByUserId(userId: Long)
}
