package com.example.lifelog.application.insight

import com.example.lifelog.domain.insight.Insight
import com.example.lifelog.domain.insight.InsightKind
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 인사이트 목록 아이템 DTO
 */
data class InsightListItem(
    val id: Long,
    val kind: InsightKind,
    val title: String,
    val body: String,
    val evidence: String?,
    val keyword: String?,
    val createdAt: Instant,
    val createdAtLabel: String?, // 필요 없으면 제거 가능
) {
    companion object {
        private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.of("Asia/Seoul"))

        fun from(
            entity: Insight,
            zone: ZoneId = ZoneId.of("Asia/Seoul"),
        ): InsightListItem {
            val createdAt = entity.createdAt
            return InsightListItem(
                id = entity.id,
                kind = entity.kind,
                title = entity.title,
                body = entity.body,
                evidence = entity.evidence,
                keyword = entity.keyword,
                createdAt = createdAt,
                createdAtLabel = fmt.format(createdAt), // UI에서 안 쓰면 null로 두거나 필드 제거
            )
        }
    }
}
