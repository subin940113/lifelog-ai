package com.example.lifelog.infrastructure.persistence.auth

import com.example.lifelog.domain.auth.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface JpaRefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByTokenHash(tokenHash: String): Optional<RefreshToken>

    fun deleteAllByUserId(userId: Long)
}
