package com.example.lifelog.application.signal

import com.example.lifelog.domain.interest.InterestRepository
import com.example.lifelog.domain.log.RawLog
import com.example.lifelog.domain.signal.KeywordSignalState
import com.example.lifelog.domain.signal.KeywordSignalStateRepository
import com.example.lifelog.domain.signal.KeywordSignalStatus
import com.example.lifelog.infrastructure.security.LogEncryption
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.Locale

/**
 * RawLogCreated 이벤트(로그 생성) 기반으로
 * ACTIVE 키워드에 대해 매칭을 수행하고 candy를 누적한다.
 *
 * 역할 분리(중요):
 * - 키워드 삭제/재생성에 따른 freeze/revive 및 waterdrop 생성/삭제는
 *   Interest(키워드) 생성/삭제 UseCase에서만 처리한다.
 * - 이 UseCase는 "로그가 생성되었을 때" candy 누적만 담당한다.
 */
@Service
class UpdateSignalUseCase(
    private val interestRepository: InterestRepository,
    private val stateRepository: KeywordSignalStateRepository,
    private val logEncryption: LogEncryption,
) {
    @Transactional
    fun execute(rawLog: RawLog) {
        val userId = rawLog.userId
        val now = Instant.now()

        // 유저의 현재 키워드(존재하는 것만)
        val keywords = interestRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
        if (keywords.isEmpty()) return

        // 로그 내용 복호화 후 normalize (InterestKeyword.keywordKey 규칙과 동일: trim + lowercase)
        val decryptedContent = logEncryption.decrypt(rawLog.content)
        val contentNormalized = normalize(decryptedContent)

        // 키워드별로 상태 조회/초기 생성 후, ACTIVE일 때만 candy 누적
        for (kw in keywords) {
            val keywordKey = kw.keywordKey

            val state =
                stateRepository.findByUserIdAndKeywordKey(userId, keywordKey)
                    ?: stateRepository.save(
                        KeywordSignalState(
                            userId = userId,
                            keywordKey = keywordKey,
                            status = KeywordSignalStatus.ACTIVE,
                            candyCount = 0,
                            createdAt = now,
                            updatedAt = now,
                        ),
                    )

            // freeze/revive는 다른 UseCase에서만 한다.
            if (state.status != KeywordSignalStatus.ACTIVE) continue

            // 단순 contains 매칭 (초기 스펙)
            if (contentNormalized.contains(keywordKey)) {
                // 정책: 1회 매칭당 candy +1
                state.addCandy(delta = 1, now = rawLog.createdAt)
                stateRepository.save(state)
            }
        }
    }

    private fun normalize(s: String): String = s.trim().lowercase(Locale.getDefault())
}
