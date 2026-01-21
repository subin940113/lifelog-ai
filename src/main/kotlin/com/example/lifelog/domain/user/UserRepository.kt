package com.example.lifelog.domain.user

/**
 * User 도메인 리포지토리 인터페이스
 * 구현체는 infrastructure 레이어에 위치
 */
interface UserRepository {
    fun save(user: User): User

    fun findById(id: Long): User?

    fun findAll(): List<User>
}
