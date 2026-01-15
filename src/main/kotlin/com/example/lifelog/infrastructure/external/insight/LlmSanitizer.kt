package com.example.lifelog.infrastructure.external.insight

import org.springframework.stereotype.Component

/**
 * LLM 입력 데이터 정제 유틸리티
 */
@Component
class LlmSanitizer {
    private val emailRegex = Regex("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}""")
    private val phoneRegex = Regex("""\b01[016789]-?\d{3,4}-?\d{4}\b""")
    private val urlRegex = Regex("""https?://\S+""")
    private val longNumberRegex = Regex("""\b\d{10,}\b""")

    fun sanitize(s: String): String {
        var x = s
        x = x.replace(emailRegex, "***@***")
        x = x.replace(phoneRegex, "***-****-****")
        x = x.replace(urlRegex, "https://***")
        x = x.replace(longNumberRegex, "****")
        return x.trim()
    }
}
