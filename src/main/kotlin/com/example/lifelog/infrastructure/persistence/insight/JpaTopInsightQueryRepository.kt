package com.example.lifelog.infrastructure.persistence.insight

import com.example.lifelog.domain.insight.TopInsightQueryRepository
import com.example.lifelog.domain.insight.TopInsightRow
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class JpaTopInsightQueryRepository(
    @PersistenceContext private val em: EntityManager,
) : TopInsightQueryRepository {
    override fun findTopLikedInsightsByUserAndKeyword(
        userId: Long,
        keywordKey: String,
        pageable: Pageable,
    ): List<TopInsightRow> {
        // NOTE: keyword는 ai_insight.keyword 컬럼 (String?) 이고, keywordKey와 동일 규칙이면 그대로 비교.
        // LIKE 기준: ai_insight_feedback.vote = 'LIKE'
        val sql =
            """
            select 
                i.id as insight_id,
                i.title as title,
                i.body as body,
                i.evidence as evidence
            from ai_insight i
            join ai_insight_feedback f
              on f.insight_id = i.id
            where i.user_id = :userId
              and i.keyword = :keyword
              and f.user_id = :userId
              and f.vote = 'LIKE'
            order by f.created_at desc, i.created_at desc
            """.trimIndent()

        val q =
            em
                .createNativeQuery(sql)
                .setParameter("userId", userId)
                .setParameter("keyword", keywordKey)
                .setFirstResult(pageable.offset.toInt())
                .setMaxResults(pageable.pageSize)

        @Suppress("UNCHECKED_CAST")
        val rows = q.resultList as List<Array<Any?>>

        return rows.map { r ->
            TopInsightRow(
                insightId = (r[0] as Number).toLong(),
                title = r[1] as String,
                body = r[2] as String,
                evidence = r[3] as String?,
            )
        }
    }
}
