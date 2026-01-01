package com.example.lifelog.user

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
    fun me(): UserMeResponse = userService.getMe()

    @PatchMapping("/me")
    fun updateMe(
        @RequestBody req: UpdateUserMeRequest,
    ): UserMeResponse = userService.updateMe(req)

    @DeleteMapping("/me")
    fun withdrawMe() {
        accountWithdrawalService.withdrawMe()
    }
}
