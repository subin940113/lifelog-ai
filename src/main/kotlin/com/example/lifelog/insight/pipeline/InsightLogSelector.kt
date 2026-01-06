package com.example.lifelog.insight.pipeline

import org.springframework.stereotype.Component
import kotlin.math.max
import kotlin.math.min

@Component
class InsightLogSelector {
    fun select(
        candidates: List<String>,
        matchedKeyword: String?,
        maxSelected: Int,
    ): List<String> {
        if (candidates.isEmpty()) return emptyList()
        val safeMax = max(1, min(maxSelected, candidates.size))

        val kw = matchedKeyword?.trim()?.takeIf { it.isNotBlank() }

        // 1) 스코어링(최근 로그가 앞에 온다고 가정: candidates[0]이 최신)
        val scored =
            candidates
                .mapIndexed { idx, text ->
                    val recencyScore = (candidates.size - idx).toDouble() // 최신일수록 높음
                    val keywordScore = if (kw != null && containsIgnoreCase(text, kw)) 1000.0 else 0.0
                    val shiftScore = emotionShiftSignalScore(text) // 0~수십
                    val lenScore = min(text.length, 200).toDouble() / 200.0 // 과도한 편향 방지

                    val total = keywordScore + recencyScore + shiftScore + lenScore
                    Scored(text = text, score = total)
                }.sortedByDescending { it.score }

        // 2) 중복 제거(완전 동일/거의 동일 제거) 후 상위 N
        val picked = ArrayList<String>(safeMax)
        for (s in scored) {
            if (picked.size >= safeMax) break
            if (isNearDuplicate(picked, s.text)) continue
            picked.add(s.text)
        }

        // 3) 너무 빡빡하게 중복 제거해서 개수가 부족하면, 남은 걸 채움(중복 허용 완화)
        if (picked.size < safeMax) {
            for (s in scored) {
                if (picked.size >= safeMax) break
                if (picked.contains(s.text)) continue
                picked.add(s.text)
            }
        }

        // 최종은 "시간 순"으로 주는 게 LLM이 읽기 편함: 오래된→최신 or 최신→오래된
        // 여기서는 프롬프트가 "most recent first"라 했으니 최신 우선 유지:
        val set = picked.toSet()
        return candidates.filter { it in set }.take(safeMax)
    }

    private data class Scored(
        val text: String,
        val score: Double,
    )

    private fun containsIgnoreCase(
        text: String,
        keyword: String,
    ): Boolean = text.lowercase().contains(keyword.lowercase())

    /**
     * 감정/상태 변화 힌트(저비용 휴리스틱)
     * - 대조/전환 접속사나 극성 단어가 섞이면 점수 상승
     */
    private fun emotionShiftSignalScore(text: String): Double {
        val t = text.lowercase()

        val pivot = listOf("근데", "하지만", "그런데", "반면", "그래도", "왔다갔다", "오락가락")
        val fatigue = listOf("피곤", "지침", "무기력", "힘들", "짜증", "우울", "불안", "걱정")
        val relief = listOf("가벼", "편안", "괜찮", "좋아", "기분", "뿌듯", "만족", "안심")

        var score = 0.0
        if (pivot.any { t.contains(it) }) score += 12.0

        val hasNeg = fatigue.any { t.contains(it) }
        val hasPos = relief.any { t.contains(it) }
        if (hasNeg && hasPos) {
            score += 18.0
        } else if (hasNeg || hasPos) {
            score += 6.0
        }

        return score
    }

    /**
     * 초저비용 near-duplicate:
     * - 공백/특수문자 제거 후 prefix 비교 + 길이/유사도 휴리스틱
     */
    private fun isNearDuplicate(
        picked: List<String>,
        candidate: String,
    ): Boolean {
        val c = normalize(candidate)
        if (c.isBlank()) return true

        for (p in picked) {
            val pp = normalize(p)
            if (pp == c) return true

            // prefix가 매우 비슷하면 중복 취급(짧은 문장에 과민반응 방지)
            val common = commonPrefixLen(pp, c)
            val minLen = min(pp.length, c.length)
            if (minLen >= 40 && common.toDouble() / minLen.toDouble() >= 0.85) return true
        }
        return false
    }

    private fun normalize(s: String): String =
        s
            .lowercase()
            .replace(Regex("""\s+"""), " ")
            .replace(Regex("""[^\p{L}\p{N}\s]"""), "")
            .trim()

    private fun commonPrefixLen(
        a: String,
        b: String,
    ): Int {
        val n = min(a.length, b.length)
        var i = 0
        while (i < n && a[i] == b[i]) i++
        return i
    }
}
