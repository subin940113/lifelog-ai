package com.example.lifelog.infrastructure.external.insight

import com.example.lifelog.application.insight.feedback.InsightPreferenceReader
import com.example.lifelog.domain.insight.InsightKind
import org.springframework.stereotype.Component

/**
 * 인사이트 관점 선택 유틸리티
 */
@Component
class InsightPerspectiveSelector(
    private val preferenceReader: InsightPreferenceReader,
) {
    fun chooseKindHint(
        userId: Long,
        selectedLogs: List<String>,
        matchedKeyword: String?,
    ): InsightKind {
        val text = selectedLogs.joinToString("\n").lowercase()

        val repetitionScore = repetitionScore(selectedLogs, matchedKeyword)
        val changeScore = changeScore(text)
        val highlightScore = highlightScore(text)
        val contrastScore = contrastScore(text)

        // base 후보들
        val candidates = mutableListOf<Pair<InsightKind, Double>>()

        if (repetitionScore >= 3) candidates += InsightKind.PATTERN to 1.0
        if (highlightScore >= 2) candidates += InsightKind.HIGHLIGHT to 0.9
        if (changeScore >= 2) candidates += InsightKind.WARNING to 0.8
        if (contrastScore >= 2) candidates += InsightKind.CONTRAST to 0.7

        if (candidates.isEmpty()) candidates += InsightKind.REFLECTION to 0.3

        // 사용자 가중치 반영
        val weighted =
            candidates.map { (kind, base) ->
                val w = preferenceReader.getKindWeight(userId, kind) // -1.5 ~ +1.5
                kind to (base + w)
            }

        return weighted.maxBy { it.second }.first
    }

    fun normalizeKind(
        userId: Long,
        modelKind: InsightKind,
        selectedLogs: List<String>,
        matchedKeyword: String?,
    ): InsightKind {
        // 기존 WARNING 과생산 방지 로직 유지 + 사용자 성향 반영
        val text = selectedLogs.joinToString("\n").lowercase()
        val changeScore = changeScore(text)
        val repetitionScore = repetitionScore(selectedLogs, matchedKeyword)
        val highlightScore = highlightScore(text)

        if (modelKind == InsightKind.WARNING && changeScore < 2) {
            return when {
                repetitionScore >= 3 -> InsightKind.PATTERN
                highlightScore >= 2 -> InsightKind.HIGHLIGHT
                else -> InsightKind.PATTERN
            }
        }

        // 사용자에게 WARNING가 싫은 kind이면 downgrade
        val w = preferenceReader.getKindWeight(userId, modelKind)
        if (w <= -0.8) {
            return when {
                highlightScore >= 2 -> InsightKind.HIGHLIGHT
                repetitionScore >= 3 -> InsightKind.PATTERN
                else -> InsightKind.REFLECTION
            }
        }

        return modelKind
    }

    private fun repetitionScore(
        logs: List<String>,
        matchedKeyword: String?,
    ): Int {
        if (logs.isEmpty()) return 0
        val key = (matchedKeyword ?: "").trim().lowercase()
        var score = 0

        // Keyword repetition
        if (key.isNotEmpty()) {
            val hits = logs.count { it.lowercase().contains(key) }
            if (hits >= 3) score += 2
            if (hits >= 6) score += 1
        }

        // Simple lexical repetition (same nouns/phrases often repeat)
        val tokens =
            logs
                .flatMap { it.lowercase().split(Regex("""\s+""")) }
                .map { it.trim() }
                .filter { it.length >= 2 }

        val freq = tokens.groupingBy { it }.eachCount()
        val top = freq.values.maxOrNull() ?: 0
        if (top >= 4) score += 2
        if (top >= 7) score += 1

        return score
    }

    private fun changeScore(text: String): Int {
        // "change" markers: earlier vs now, suddenly, recently, not like before, etc.
        val markers =
            listOf(
                "요즘",
                "최근",
                "갑자기",
                "원래",
                "예전",
                "전에는",
                "이전엔",
                "달라",
                "변화",
                "안 하던",
                "못 하던",
                "잘 안",
                "안 와",
                "안 옴",
                "lately",
                "recently",
                "suddenly",
                "used to",
                "not anymore",
                "no longer",
                "different",
            )
        return markers.count { text.contains(it) }.coerceAtMost(4)
    }

    private fun highlightScore(text: String): Int {
        // vivid / concrete moment cues (cute moment, direct interaction, specific scene)
        val markers =
            listOf(
                "오늘",
                "방금",
                "내 옆",
                "찾아와",
                "야옹",
                "뽀뽀",
                "안고",
                "애교",
                "today",
                "just",
                "came",
                "next to me",
                "kiss",
                "hug",
            )
        return markers.count { text.contains(it) }.coerceAtMost(4)
    }

    private fun contrastScore(text: String): Int {
        // coexistence cues: but/however/though, ~지만, ~한데
        val markers = listOf("하지만", "근데", "그런데", "반면", "그러나", "yet", "but", "however", "though", "while")
        return markers.count { text.contains(it) }.coerceAtMost(4)
    }
}
