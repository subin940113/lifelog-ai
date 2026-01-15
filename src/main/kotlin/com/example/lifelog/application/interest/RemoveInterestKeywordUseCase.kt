package com.example.lifelog.application.interest

import com.example.lifelog.domain.interest.InterestRepository
import com.example.lifelog.presentation.api.interest.InterestResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 관심사 키워드 제거 Use Case
 */
@Service
class RemoveInterestKeywordUseCase(
    private val interestRepository: InterestRepository,
    private val getInterestsUseCase: GetInterestsUseCase,
) {
    @Transactional
    fun execute(
        userId: Long,
        keywordRaw: String,
    ): InterestResponse {
        val keyword = normalize(keywordRaw)
        if (keyword.isBlank()) return getInterestsUseCase.execute(userId)

        val keywordKey = keywordKey(keyword)

        // hard delete
        val interestKeyword = interestRepository.findByUserIdAndKeywordKey(userId, keywordKey)
        if (interestKeyword != null) {
            interestRepository.delete(interestKeyword)
        }

        return getInterestsUseCase.execute(userId)
    }

    private fun normalize(input: String?): String = (input ?: "").trim()

    private fun keywordKey(keyword: String): String = keyword.trim().lowercase()
}
