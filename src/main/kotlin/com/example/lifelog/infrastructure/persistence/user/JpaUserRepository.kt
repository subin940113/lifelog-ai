package com.example.lifelog.infrastructure.persistence.user

import com.example.lifelog.domain.user.User
import com.example.lifelog.domain.user.UserRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * User 도메인 리포지토리 JPA 구현체
 */
@Repository
interface JpaUserRepository : JpaRepository<User, Long>, UserRepository
