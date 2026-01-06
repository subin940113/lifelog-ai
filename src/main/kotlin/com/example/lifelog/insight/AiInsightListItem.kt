package com.example.lifelog.insight

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class AiInsightListItem(
    val id: Long,
    val kind: AiInsightKind,
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
            entity: AiInsight,
            zone: ZoneId = ZoneId.of("Asia/Seoul"),
        ): AiInsightListItem {
            val createdAt = entity.createdAt
            return AiInsightListItem(
                id = entity.id ?: 0L,
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
