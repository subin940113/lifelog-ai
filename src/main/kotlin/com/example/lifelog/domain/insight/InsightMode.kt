package com.example.lifelog.domain.insight

/**
 * 인사이트 생성 모드
 */
enum class InsightMode {
    SINGLE,
    WINDOW,
    ;

    companion object {
        fun parse(raw: String?): InsightMode {
            val v = raw?.trim()?.uppercase()
            return when (v) {
                "SINGLE", "MODE1" -> SINGLE
                "WINDOW", "MODE2" -> WINDOW
                else -> WINDOW
            }
        }
    }
}
