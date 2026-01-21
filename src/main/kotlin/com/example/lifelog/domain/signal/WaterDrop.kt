package com.example.lifelog.domain.signal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

/**
 * 영구 물방울(정책):
 * - "인사이트 발생"이 아니라 "키워드 삭제로 인해 더 이상 발전할 수 없게 되었을 때" 생성되는 스냅샷 오브젝트
 * - 동일 키워드가 다시 생성되면 삭제(권장)
 */
@Entity
@Table(
    name = "water_drop",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_water_drop_user_keyword_key",
            columnNames = ["user_id", "keyword_key"],
        ),
    ],
    indexes = [
        Index(name = "idx_water_drop_user_created", columnList = "user_id, created_at"),
    ],
)
class WaterDrop(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "user_id", nullable = false)
    val userId: Long,
    @Column(name = "keyword_key", nullable = false, length = 80)
    val keywordKey: String,
    /**
     * freeze 시점(생성 시점)
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),
    /**
     * 최종 스냅샷 요약(초기엔 null 허용, 나중에 LLM 결과 저장)
     */
    @Column(name = "snapshot_text", columnDefinition = "text")
    var snapshotText: String? = null,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
) {
    fun touch(now: Instant = Instant.now()) {
        updatedAt = now
    }
}
