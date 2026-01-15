package com.example.lifelog.application.interest

import com.example.lifelog.domain.interest.InterestRepository
import com.example.lifelog.presentation.api.interest.InterestResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 관심사 조회 Use Case
 */
@Service
class GetInterestsUseCase(
    private val interestRepository: InterestRepository,
) {
    @Transactional(readOnly = true)
    fun execute(userId: Long): InterestResponse {
        val keywords = interestRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
        return InterestResponse(
            keywords = keywords.map { it.keyword },
        )
    }
}
