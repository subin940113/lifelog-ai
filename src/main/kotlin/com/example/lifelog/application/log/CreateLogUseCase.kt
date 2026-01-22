package com.example.lifelog.application.log

import com.example.lifelog.common.exception.ErrorCode
import com.example.lifelog.common.exception.ValidationException
import com.example.lifelog.domain.log.LogRepository
import com.example.lifelog.domain.log.RawLog
import com.example.lifelog.domain.log.RawLogCreatedEvent
import com.example.lifelog.infrastructure.security.LogEncryption
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 로그 생성 Use Case
 */
@Service
class CreateLogUseCase(
    private val logRepository: LogRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val logEncryption: LogEncryption,
) {
    @Transactional
    fun execute(
        userId: Long,
        content: String,
    ): RawLog {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) {
            throw ValidationException(ErrorCode.VALIDATION_BLANK_CONTENT)
        }

        // 로그 내용 암호화
        val encryptedContent = logEncryption.encrypt(trimmed)

        val log =
            RawLog(
                userId = userId,
                content = encryptedContent,
            )

        val saved = logRepository.save(log)

        // 트랜잭션 커밋 이후 리스너에서 받도록 이벤트 발행
        eventPublisher.publishEvent(RawLogCreatedEvent(saved))

        return saved
    }
}
