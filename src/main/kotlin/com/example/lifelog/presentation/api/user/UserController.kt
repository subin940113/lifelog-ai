package com.example.lifelog.presentation.api.user

import com.example.lifelog.application.user.GetUserUseCase
import com.example.lifelog.application.user.UpdateUserRequest
import com.example.lifelog.application.user.UpdateUserUseCase
import com.example.lifelog.application.user.UserResponse
import com.example.lifelog.application.user.WithdrawUserUseCase
import com.example.lifelog.infrastructure.security.AuthPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 사용자 API Controller
 */
@RestController
@RequestMapping("/api/users")
class UserController(
    private val getUserUseCase: GetUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val withdrawUserUseCase: WithdrawUserUseCase,
) {
    @GetMapping("/me")
    fun getMe(
        @AuthenticationPrincipal principal: AuthPrincipal,
    ): UserResponse = getUserUseCase.execute(principal.userId)

    @PatchMapping("/me")
    fun updateMe(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @RequestBody request: UpdateUserRequest,
    ): UserResponse =
        updateUserUseCase.execute(
            userId = principal.userId,
            request = request,
        )

    @DeleteMapping("/me")
    fun withdrawMe(
        @AuthenticationPrincipal principal: AuthPrincipal,
    ) {
        withdrawUserUseCase.execute(principal.userId)
    }
}
