package com.example.lifelog.infrastructure.security

import org.springframework.security.core.context.SecurityContextHolder

/**
 * 인증 컨텍스트 유틸리티
 */
object AuthContext {
    fun currentUserId(): Long {
        val auth =
            SecurityContextHolder.getContext().authentication
                ?: throw IllegalStateException("Unauthenticated")

        val principal = auth.principal
        if (principal !is AuthPrincipal) {
            throw IllegalStateException("Unexpected principal type: ${principal?.javaClass?.name}")
        }

        return principal.userId
    }
}
