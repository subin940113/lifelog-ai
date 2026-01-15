package com.example.lifelog.domain.insight.feedback

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant

/**
 * 인사이트 피드백 도메인 엔티티
 */
@Entity
@Table(
    name = "ai_insight_feedback",
    indexes = [
        Index(name = "idx_feedback_user_created", columnList = "userId, createdAt"),
        Index(name = "idx_feedback_insight", columnList = "insightId"),
    ],
)
class InsightFeedback(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val userId: Long,
    val insightId: Long,
    @Enumerated(EnumType.STRING)
    var vote: FeedbackVote, // LIKE / DISLIKE
    @Enumerated(EnumType.STRING)
    var reason: FeedbackReason? = null, // nullable
    var score: Int? = null, // 1~5 optional
    @Column(length = 1000)
    var comment: String? = null,
    val createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now(),
) {
    fun update(
        vote: FeedbackVote,
        reason: FeedbackReason?,
        score: Int?,
        comment: String?,
    ) {
        this.vote = vote
        this.reason = reason
        this.score = score
        this.comment = comment
        this.updatedAt = Instant.now()
    }
}

enum class FeedbackVote { LIKE, DISLIKE }

enum class FeedbackReason {
    TOO_OBVIOUS,
    DUPLICATE,
    TOO_SHARP,
    NOT_RELEVANT,
    BAD_TIMING,
    OTHER,
}
