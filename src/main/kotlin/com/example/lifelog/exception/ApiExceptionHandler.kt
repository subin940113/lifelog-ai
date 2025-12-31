package com.example.lifelog.exception

import com.example.lifelog.auth.UnauthorizedException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(UnauthorizedException::class)
    fun unauthorized(e: UnauthorizedException): ResponseEntity<Map<String, String>> =
        ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(mapOf("message" to (e.message ?: "unauthorized")))
}
