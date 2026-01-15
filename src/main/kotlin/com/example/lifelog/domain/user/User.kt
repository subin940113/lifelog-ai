package com.example.lifelog.domain.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

/**
 * 사용자 도메인 엔티티
 */
@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(name = "display_name", nullable = false)
    var displayName: String,
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),
    @Column(name = "last_login_at", nullable = false, updatable = true)
    var lastLoginAt: Instant = Instant.now(),
    @Column(name = "deleted_at", nullable = true, updatable = true)
    var deletedAt: Instant? = null,
) {
    /**
     * 로그인 시간 업데이트
     */
    fun updateLastLoginAt() {
        lastLoginAt = Instant.now()
    }

    /**
     * 표시 이름 업데이트
     */
    fun updateDisplayName(displayName: String) {
        require(displayName.isNotBlank()) { "displayName cannot be blank" }
        this.displayName = displayName
    }

    /**
     * 사용자 삭제 (소프트 삭제)
     */
    fun delete() {
        deletedAt = Instant.now()
    }

    /**
     * 사용자가 삭제되었는지 확인
     */
    fun isDeleted(): Boolean {
        return deletedAt != null
    }
}
