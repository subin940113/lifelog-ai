package com.example.lifelog.infrastructure.security

import com.example.lifelog.common.exception.ErrorCode
import com.example.lifelog.common.exception.UnauthorizedException
import org.springframework.security.core.context.SecurityContextHolder

/**
 * 인증 컨텍스트 유틸리티
 */
object AuthContext {
    fun currentUserId(): Long {
        val auth =
            SecurityContextHolder.getContext().authentication
                ?: throw UnauthorizedException(ErrorCode.UNAUTHORIZED, "Unauthenticated")

        val principal = auth.principal
        if (principal !is AuthPrincipal) {
            throw UnauthorizedException(ErrorCode.UNAUTHORIZED, "Unexpected principal type: ${principal?.javaClass?.name}")
        }

        return principal.userId
    }
}
