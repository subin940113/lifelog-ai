package com.example.lifelog.insight.feedback

import com.example.lifelog.insight.AiInsightKind
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

@Entity
@Table(
    name = "insight_preference_profile",
    uniqueConstraints = [UniqueConstraint(name = "uq_pref_user", columnNames = ["userId"])],
)
class InsightPreferenceProfile(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val userId: Long,
    // kind별 가중치(간단히 JSON 문자열로 저장)
    @Column(length = 2000)
    var kindWeightsJson: String,
    // 생성 빈도 조절용 (싫어요 연속/최근 품질)
    var dislikeStreak: Int = 0,
    var updatedAt: Instant = Instant.now(),
)

data class KindWeight(
    val kind: AiInsightKind,
    val weight: Double,
)
