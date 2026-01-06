package com.example.lifelog.interest

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import kotlin.math.min

@Service
class InterestService(
    private val repo: InterestSettingsRepository,
) {
    companion object {
        private const val MAX_KEYWORDS = 5
    }

    @Transactional(readOnly = true)
    fun getOrDefault(userId: Long): InterestStateResponse {
        val s = repo.findByUserId(userId)
        return InterestStateResponse(
            enabled = s?.enabled ?: false,
            keywords = (s?.keywords ?: emptyList()),
        )
    }

    @Transactional
    fun setEnabled(
        userId: Long,
        enabled: Boolean,
    ): InterestStateResponse {
        val now = Instant.now()
        val s = repo.findByUserId(userId)
        if (s != null) {
            s.enabled = enabled
            s.updatedAt = now
            repo.save(s)
            return InterestStateResponse(enabled = s.enabled, keywords = s.keywords)
        }

        val created =
            repo.save(
                InterestSettings(
                    userId = userId,
                    enabled = enabled,
                    keywords = emptyList(),
                    updatedAt = now,
                ),
            )
        return InterestStateResponse(enabled = created.enabled, keywords = created.keywords)
    }

    @Transactional
    fun addKeyword(
        userId: Long,
        keywordRaw: String,
    ): InterestStateResponse {
        val now = Instant.now()
        val keyword = normalize(keywordRaw)

        if (keyword.isBlank()) {
            throw IllegalArgumentException("keyword는 비어있을 수 없습니다.")
        }

        val s =
            repo.findByUserId(userId) ?: InterestSettings(
                userId = userId,
                enabled = false,
                keywords = emptyList(),
                updatedAt = now,
            )

        val current = s.keywords
        if (current.size >= MAX_KEYWORDS) {
            throw IllegalArgumentException("관심사는 최대 ${MAX_KEYWORDS}개까지 등록할 수 있어요.")
        }

        val exists = current.any { equalsIgnoreCase(it, keyword) }
        if (exists) {
            // UX적으로 “그대로 반환”이 더 자연스러움(중복이라도 상태는 유지)
            return InterestStateResponse(enabled = s.enabled, keywords = current)
        }

        val next = (current + keyword).take(min(MAX_KEYWORDS, current.size + 1))

        s.keywords = next
        s.updatedAt = now
        repo.save(s)

        return InterestStateResponse(enabled = s.enabled, keywords = s.keywords)
    }

    @Transactional
    fun removeKeyword(
        userId: Long,
        keywordRaw: String,
    ): InterestStateResponse {
        val now = Instant.now()
        val keyword = normalize(keywordRaw)

        val s =
            repo.findByUserId(userId) ?: return InterestStateResponse(
                enabled = false,
                keywords = emptyList(),
            )

        val next = s.keywords.filterNot { equalsIgnoreCase(it, keyword) }

        if (next == s.keywords) {
            // 없던 키워드를 지우려 해도 상태는 그대로 반환
            return InterestStateResponse(enabled = s.enabled, keywords = s.keywords)
        }

        s.keywords = next
        s.updatedAt = now
        repo.save(s)

        return InterestStateResponse(enabled = s.enabled, keywords = s.keywords)
    }

    private fun normalize(s: String?): String = (s ?: "").trim()

    private fun equalsIgnoreCase(
        a: String,
        b: String,
    ): Boolean = a.trim().equals(b.trim(), ignoreCase = true)
}
