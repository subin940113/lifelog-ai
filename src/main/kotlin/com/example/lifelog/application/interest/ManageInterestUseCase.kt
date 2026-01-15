package com.example.lifelog.application.interest

import com.example.lifelog.domain.interest.InterestKeyword
import com.example.lifelog.domain.interest.InterestRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 관심사 관리 Use Case
 */
@Service
class ManageInterestUseCase(
    private val interestRepository: InterestRepository,
) {
    companion object {
        private const val MAX_KEYWORDS = 5
    }

    @Transactional(readOnly = true)
    fun getInterests(userId: Long): InterestResponse {
        val keywords = interestRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
        return InterestResponse(
            keywords = keywords.map { it.keyword },
        )
    }

    @Transactional
    fun addKeyword(
        userId: Long,
        keywordRaw: String,
    ): InterestResponse {
        val keyword = normalize(keywordRaw)
        require(keyword.isNotBlank()) { "keyword는 비어있을 수 없습니다." }

        val key = keywordKey(keyword)

        // 이미 있으면 그대로 반환 (UX: 중복 등록해도 에러보단 무시)
        if (interestRepository.existsByUserIdAndKeywordKey(userId, key)) {
            return getInterests(userId)
        }

        val count = interestRepository.countByUserId(userId).toInt()
        require(count < MAX_KEYWORDS) { "관심사는 최대 ${MAX_KEYWORDS}개까지 등록할 수 있어요." }

        interestRepository.save(
            InterestKeyword(
                userId = userId,
                keyword = keyword,
                keywordKey = key,
            ),
        )

        return getInterests(userId)
    }

    @Transactional
    fun removeKeyword(
        userId: Long,
        keywordRaw: String,
    ): InterestResponse {
        val keyword = normalize(keywordRaw)
        if (keyword.isBlank()) return getInterests(userId)

        val keywordKey = keywordKey(keyword)

        // hard delete
        val interestKeyword = interestRepository.findByUserIdAndKeywordKey(userId, keywordKey)
        if (interestKeyword != null) {
            interestRepository.delete(interestKeyword)
        }

        return getInterests(userId)
    }

    private fun normalize(input: String?): String = (input ?: "").trim()

    private fun keywordKey(keyword: String): String = keyword.trim().lowercase()
}

/**
 * 관심사 응답 DTO
 */
data class InterestResponse(
    val keywords: List<String>,
)
