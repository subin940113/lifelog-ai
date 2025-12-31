package com.example.lifelog.auth.security

import org.springframework.security.core.context.SecurityContextHolder

object AuthContext {
    fun currentUserId(): Long {
        val auth = SecurityContextHolder.getContext().authentication
            ?: throw IllegalStateException("Unauthenticated")

        val principal = auth.principal
        if (principal !is AuthPrincipal) {
            throw IllegalStateException("Unexpected principal type: ${principal?.javaClass?.name}")
        }

        return principal.userId
    }
}