package com.example.lifelog.application.interest

import com.example.lifelog.application.signal.ReviveKeywordSignalStateUseCase
import com.example.lifelog.common.exception.BusinessException
import com.example.lifelog.common.exception.ErrorCode
import com.example.lifelog.common.exception.ValidationException
import com.example.lifelog.domain.interest.InterestKeyword
import com.example.lifelog.domain.interest.InterestRepository
import com.example.lifelog.presentation.api.interest.InterestResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 관심사 키워드 추가 Use Case
 */
@Service
class AddInterestKeywordUseCase(
    private val interestRepository: InterestRepository,
    private val getInterestsUseCase: GetInterestsUseCase,
    private val reviveKeywordSignalStateUseCase: ReviveKeywordSignalStateUseCase,
) {
    companion object {
        private const val MAX_KEYWORDS = 5
    }

    @Transactional
    fun execute(
        userId: Long,
        keywordRaw: String,
    ): InterestResponse {
        val keyword = normalize(keywordRaw)
        if (keyword.isBlank()) {
            throw ValidationException(ErrorCode.VALIDATION_BLANK_KEYWORD)
        }

        val key = keywordKey(keyword)

        // 이미 있으면 그대로 반환 (UX: 중복 등록해도 에러보단 무시)
        if (interestRepository.existsByUserIdAndKeywordKey(userId, key)) {
            return getInterestsUseCase.execute(userId)
        }

        val count = interestRepository.countByUserId(userId).toInt()
        if (count >= MAX_KEYWORDS) {
            throw BusinessException(ErrorCode.BUSINESS_MAX_INTERESTS_EXCEEDED)
        }

        interestRepository.save(
            InterestKeyword(
                userId = userId,
                keyword = keyword,
                keywordKey = key,
            ),
        )

        reviveKeywordSignalStateUseCase.execute(userId, key)

        return getInterestsUseCase.execute(userId)
    }

    private fun normalize(input: String?): String = (input ?: "").trim()

    private fun keywordKey(keyword: String): String = keyword.trim().lowercase()
}
