package com.example.lifelog.auth

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByTokenHash(tokenHash: String): Optional<RefreshToken>

    fun deleteAllByUserId(userId: Long)
}
