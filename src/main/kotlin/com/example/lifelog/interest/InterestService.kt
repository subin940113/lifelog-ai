package com.example.lifelog.interest

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InterestService(
    private val repo: InterestKeywordRepository,
) {
    companion object {
        private const val MAX_KEYWORDS = 5
    }

    @Transactional(readOnly = true)
    fun get(userId: Long): InterestStateResponse {
        val rows = repo.findAllByUserIdOrderByCreatedAtDesc(userId)
        return InterestStateResponse(
            keywords = rows.map { it.keyword },
        )
    }

    @Transactional
    fun addKeyword(
        userId: Long,
        keywordRaw: String,
    ): InterestStateResponse {
        val keyword = normalize(keywordRaw)
        if (keyword.isBlank()) throw IllegalArgumentException("keyword는 비어있을 수 없습니다.")

        val key = keywordKey(keyword)

        // 이미 있으면 그대로 반환 (UX: 중복 등록해도 에러보단 무시)
        if (repo.existsByUserIdAndKeywordKey(userId, key)) {
            return get(userId)
        }

        val count = repo.countByUserId(userId).toInt()
        if (count >= MAX_KEYWORDS) {
            throw IllegalArgumentException("관심사는 최대 ${MAX_KEYWORDS}개까지 등록할 수 있어요.")
        }

        repo.save(
            InterestKeyword(
                userId = userId,
                keyword = keyword,
                keywordKey = key,
            ),
        )

        return get(userId)
    }

    @Transactional
    fun removeKeyword(
        userId: Long,
        keywordRaw: String,
    ): InterestStateResponse {
        val keyword = normalize(keywordRaw)
        if (keyword.isBlank()) return get(userId)

        val key = keywordKey(keyword)

        // hard delete
        val row = repo.findByUserIdAndKeywordKey(userId, key)
        if (row != null) {
            repo.delete(row)
        }

        return get(userId)
    }

    private fun normalize(s: String?): String = (s ?: "").trim()

    private fun keywordKey(keyword: String): String = keyword.trim().lowercase()
}
