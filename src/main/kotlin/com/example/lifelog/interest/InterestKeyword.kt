package com.example.lifelog.interest

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

@Entity
@Table(
    name = "interest_keyword",
    uniqueConstraints = [
        // 유저별 키워드 중복 방지(대소문자 무시를 위해 keywordKey 사용)
        UniqueConstraint(name = "uk_interest_keyword_user_keyword_key", columnNames = ["user_id", "keyword_key"]),
    ],
    indexes = [
        Index(name = "idx_interest_keyword_user_created", columnList = "user_id, created_at"),
    ],
)
class InterestKeyword(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "user_id", nullable = false)
    val userId: Long,
    // 원문 표시용
    @Column(name = "keyword", nullable = false, length = 80)
    val keyword: String,
    // 중복 판단용(소문자/trim)
    @Column(name = "keyword_key", nullable = false, length = 80)
    val keywordKey: String,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),
)
