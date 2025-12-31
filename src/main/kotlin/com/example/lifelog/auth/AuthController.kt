package com.example.lifelog.auth

import jakarta.validation.constraints.NotBlank
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
) {
    data class GoogleLoginRequest(
        @field:NotBlank val idToken: String,
    )

    @PostMapping("/oauth/google")
    fun google(
        @RequestBody req: GoogleLoginRequest,
    ): GoogleLoginResult {
        print(req.idToken)
        return authService.loginGoogle(req.idToken)
    }
}
