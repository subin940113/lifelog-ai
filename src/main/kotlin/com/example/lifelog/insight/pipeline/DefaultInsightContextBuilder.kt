package com.example.lifelog.insight.pipeline

import com.example.lifelog.log.raw.RawLog
import com.example.lifelog.log.raw.RawLogRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component

@Component
class DefaultInsightContextBuilder(
    private val rawLogRepository: RawLogRepository,
    private val props: InsightPolicyProperties,
) : InsightContextBuilder {
    override fun build(
        userId: Long,
        rawLog: RawLog,
        matchedKeyword: String?,
    ): InsightContext {
        val candidates = loadCandidates(userId, props.candidateWindowSize.coerceIn(1, 200))

        return InsightContext(
            userId = userId,
            matchedKeyword = matchedKeyword,
            triggerLog = rawLog,
            sourceLogId = rawLog.id,
            logs = candidates,
        )
    }

    private fun loadCandidates(
        userId: Long,
        size: Int,
    ): List<RawLog> {
        val pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        return rawLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
    }
}
