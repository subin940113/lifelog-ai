package com.example.lifelog.infrastructure.external.insight

import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneId
import kotlin.math.max
import kotlin.math.min

/**
 * 인사이트 생성을 위한 로그 선택 유틸리티
 */
@Component
class InsightLogSelector {
    data class LogCandidate(
        val content: String,
        val createdAt: Instant?,
    )

    fun select(
        candidates: List<LogCandidate>,
        matchedKeyword: String?,
        maxSelected: Int,
    ): List<String> {
        if (candidates.isEmpty()) return emptyList()
        val safeMax = max(1, min(maxSelected, candidates.size))

        val kw = matchedKeyword?.trim()?.takeIf { it.isNotBlank() }?.lowercase()

        // 1) keyword-hit / non-hit 버킷 분리
        val (hit, nonHit) =
            if (kw == null) {
                candidates to emptyList()
            } else {
                candidates.partition { it.content.lowercase().contains(kw) }
            }

        // 2) keyword 쏠림 제한 (기본 60%)
        val hitQuota = min((safeMax * 0.6).toInt().coerceAtLeast(1), hit.size)
        val nonQuota = min(safeMax - hitQuota, nonHit.size)

        val picked = ArrayList<LogCandidate>(safeMax)

        picked += pickWithTimeSpread(hit, hitQuota)
        picked += pickWithTimeSpread(nonHit, nonQuota)

        // 3) 부족하면 전체에서 채움
        if (picked.size < safeMax) {
            val remaining =
                candidates.filterNot { c -> picked.any { it.content == c.content } }
            picked += pickWithTimeSpread(remaining, safeMax - picked.size)
        }

        // 4) 최종 출력은 "most recent first" 유지
        val chosen = picked.map { it.content }.toSet()
        return candidates.map { it.content }.filter { it in chosen }.take(safeMax)
    }

    private fun pickWithTimeSpread(
        pool: List<LogCandidate>,
        limit: Int,
    ): List<LogCandidate> {
        if (limit <= 0 || pool.isEmpty()) return emptyList()

        val hasTime = pool.any { it.createdAt != null }

        val scored =
            pool
                .mapIndexed { idx, c ->
                    val text = c.content

                    val shiftScore = emotionShiftSignalScore(text) // 변화/대비 신호
                    val lenScore = min(text.length, 200).toDouble() / 200.0

                    // createdAt 있으면 시간 기반 가중, 없으면 index 기반
                    val recencyScore =
                        if (c.createdAt != null) {
                            c.createdAt.epochSecond.toDouble() / 1_000_000.0
                        } else {
                            (pool.size - idx).toDouble()
                        }

                    val total = (recencyScore * 1.0) + (shiftScore * 1.2) + (lenScore * 0.3)
                    Scored(c, total)
                }.sortedByDescending { it.score }

        val picked = ArrayList<LogCandidate>(limit)
        val usedBuckets = HashSet<String>()

        for (s in scored) {
            if (picked.size >= limit) break
            if (isNearDuplicate(picked.map { it.content }, s.candidate.content)) continue

            if (hasTime && s.candidate.createdAt != null) {
                val bucket = timeBucket(s.candidate.createdAt)
                if (bucket in usedBuckets) continue
                usedBuckets.add(bucket)
            }

            picked.add(s.candidate)
        }

        // 버킷 때문에 못 채웠으면 완화해서 채움
        if (picked.size < limit) {
            for (s in scored) {
                if (picked.size >= limit) break
                if (picked.any { it.content == s.candidate.content }) continue
                if (isNearDuplicate(picked.map { it.content }, s.candidate.content)) continue
                picked.add(s.candidate)
            }
        }

        return picked
    }

    private data class Scored(
        val candidate: LogCandidate,
        val score: Double,
    )

    private fun timeBucket(t: Instant): String {
        val z = t.atZone(ZoneId.of("Asia/Seoul"))
        val day = z.toLocalDate().toString()
        val hour = z.hour

        val slot =
            when (hour) {
                in 0..5 -> "NIGHT"
                in 6..11 -> "AM"
                in 12..17 -> "PM"
                else -> "EVENING"
            }

        return "$day#$slot"
    }

    /**
     * 감정/상태 변화 힌트(저비용 휴리스틱)
     * - 대조/전환 접속사나 극성 단어가 섞이면 점수 상승
     */
    private fun emotionShiftSignalScore(text: String): Double {
        val t = text.lowercase()

        val pivot = listOf("근데", "하지만", "그런데", "반면", "그래도", "왔다갔다", "오락가락", "but", "however", "though", "yet")
        val fatigue = listOf("피곤", "지침", "무기력", "힘들", "짜증", "우울", "불안", "걱정", "tired", "anx", "worry", "sad")
        val relief = listOf("가벼", "편안", "괜찮", "좋아", "기분", "뿌듯", "만족", "안심", "fine", "okay", "good", "relief")

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
