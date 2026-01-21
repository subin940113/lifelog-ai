package com.example.lifelog.domain.log

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

/**
 * 사용자가 입력한 자연어 기반 로그 (도메인 엔티티)
 */
@Entity
@Table(name = "raw_log")
class RawLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "user_id", nullable = false)
    val userId: Long,
    @Column(nullable = false, columnDefinition = "text")
    val content: String,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),
) {
    /**
     * 로그 내용의 미리보기 생성
     */
    fun preview(maxLength: Int = 140): String {
        val singleLine = content.replace("\n", " ").trim()
        return if (singleLine.length <= maxLength) {
            singleLine
        } else {
            singleLine.substring(0, maxLength).trimEnd() + "…"
        }
    }
}
