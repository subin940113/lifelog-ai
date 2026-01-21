package com.example.lifelog.domain.signal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

@Entity
@Table(
    name = "keyword_signal_state",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_keyword_signal_state_user_keyword_key",
            columnNames = ["user_id", "keyword_key"],
        ),
    ],
    indexes = [
        Index(name = "idx_keyword_signal_state_user_status", columnList = "user_id, status"),
        Index(name = "idx_keyword_signal_state_user_updated", columnList = "user_id, updated_at"),
        Index(name = "idx_keyword_signal_state_user_insight_updated", columnList = "user_id, insight_updated_at"),
    ],
)
class KeywordSignalState(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "user_id", nullable = false)
    val userId: Long,
    @Column(name = "keyword_key", nullable = false, length = 80)
    val keywordKey: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    var status: KeywordSignalStatus = KeywordSignalStatus.ACTIVE,
    @Column(name = "candy_count", nullable = false)
    var candyCount: Long = 0,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
    @Column(name = "frozen_at")
    var frozenAt: Instant? = null,
    // ---------------------------
    // Insight fields (NEW)
    // ---------------------------
    @Column(name = "insight_text", columnDefinition = "text")
    var insightText: String? = null,
    @Column(name = "insight_updated_at")
    var insightUpdatedAt: Instant? = null,
    @Column(name = "insight_version", nullable = false)
    var insightVersion: Long = 0,
    /**
     * 마지막 인사이트 생성 시점의 candyCount 스냅샷
     * - candyCount가 늘어도 매번 인사이트 생성하지 않기 위한 체크포인트
     */
    @Column(name = "insight_candy_checkpoint", nullable = false)
    var insightCandyCheckpoint: Long = 0,
) {
    fun addCandy(
        delta: Long,
        now: Instant = Instant.now(),
    ) {
        if (status != KeywordSignalStatus.ACTIVE) return
        if (delta <= 0) return
        candyCount += delta
        updatedAt = now
    }

    fun freeze(now: Instant = Instant.now()) {
        if (status == KeywordSignalStatus.FROZEN) return
        status = KeywordSignalStatus.FROZEN
        frozenAt = now
        updatedAt = now
    }

    fun revive(now: Instant = Instant.now()) {
        if (status == KeywordSignalStatus.ACTIVE) return
        status = KeywordSignalStatus.ACTIVE
        frozenAt = null
        updatedAt = now
        // revive 시 인사이트는 유지(정책상 삭제되지 않음)
        // 필요하면 UI에서 '업데이트 가능' 상태만 바뀐다.
    }

    fun shouldRegenerateInsight(thresholdDelta: Long): Boolean {
        if (status != KeywordSignalStatus.ACTIVE) return false
        val delta = candyCount - insightCandyCheckpoint
        return delta >= thresholdDelta
    }

    fun applyInsight(
        text: String,
        now: Instant = Instant.now(),
    ) {
        insightText = text
        insightUpdatedAt = now
        insightVersion += 1
        insightCandyCheckpoint = candyCount
        updatedAt = now
    }
}
