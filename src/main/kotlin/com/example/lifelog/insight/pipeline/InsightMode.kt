package com.example.lifelog.insight.pipeline

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
