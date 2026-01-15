package com.example.lifelog.user

import com.example.lifelog.auth.security.AuthPrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val accountWithdrawalService: AccountWithdrawalService,
) {
    @GetMapping("/me")
    fun me(
        @AuthenticationPrincipal principal: AuthPrincipal,
    ): UserMeResponse = userService.getMe(principal.userId)

    @PatchMapping("/me")
    fun updateMe(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @RequestBody req: UpdateUserMeRequest,
    ): UserMeResponse = userService.updateMe(principal.userId, req)

    @DeleteMapping("/me")
    fun withdrawMe(
        @AuthenticationPrincipal principal: AuthPrincipal,
    ) {
        accountWithdrawalService.withdrawMe(principal.userId)
    }
}
