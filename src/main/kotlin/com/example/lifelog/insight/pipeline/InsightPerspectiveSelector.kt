package com.example.lifelog.insight.pipeline

import com.example.lifelog.insight.AiInsightKind
import org.springframework.stereotype.Component

@Component
class InsightPerspectiveSelector {
    /**
     * Return a perspective hint that is least burdensome by default.
     * Priority: PATTERN > HIGHLIGHT > WARNING > CONTRAST > REFLECTION > QUESTION
     */
    fun chooseKindHint(
        selectedLogs: List<String>,
        matchedKeyword: String?,
    ): AiInsightKind {
        val text = selectedLogs.joinToString("\n").lowercase()

        val repetitionScore = repetitionScore(selectedLogs, matchedKeyword)
        val changeScore = changeScore(text)
        val highlightScore = highlightScore(text)
        val contrastScore = contrastScore(text)

        // Least burdensome first, but require a minimum signal
        if (repetitionScore >= 3) return AiInsightKind.PATTERN
        if (highlightScore >= 2) return AiInsightKind.HIGHLIGHT

        // WARNING is only a hint when "change" signals are actually present
        if (changeScore >= 2) return AiInsightKind.WARNING

        if (contrastScore >= 2) return AiInsightKind.CONTRAST

        // Fallbacks: keep these rare on Home
        return AiInsightKind.REFLECTION
    }

    /**
     * Post-validate / soften kind to avoid WARNING overproduction.
     * If model returns WARNING without change signals, downgrade to PATTERN or HIGHLIGHT.
     */
    fun normalizeKind(
        modelKind: AiInsightKind,
        selectedLogs: List<String>,
        matchedKeyword: String?,
    ): AiInsightKind {
        val text = selectedLogs.joinToString("\n").lowercase()
        val changeScore = changeScore(text)
        val repetitionScore = repetitionScore(selectedLogs, matchedKeyword)
        val highlightScore = highlightScore(text)

        if (modelKind == AiInsightKind.WARNING && changeScore < 2) {
            return when {
                repetitionScore >= 3 -> AiInsightKind.PATTERN
                highlightScore >= 2 -> AiInsightKind.HIGHLIGHT
                else -> AiInsightKind.PATTERN
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
        // “change” markers: earlier vs now, suddenly, recently, not like before, etc.
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
