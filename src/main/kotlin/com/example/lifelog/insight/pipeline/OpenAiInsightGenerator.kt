package com.example.lifelog.insight.pipeline

import com.example.lifelog.infra.openai.OpenAiClient
import org.springframework.stereotype.Component

@Component
class OpenAiInsightGenerator(
    private val openAiClient: OpenAiClient,
    private val promptLoader: InsightPromptLoader,
    private val sanitizer: LlmSanitizer,
    private val parser: InsightLlmParser,
    private val selector: InsightLogSelector,
    private val perspectiveSelector: InsightPerspectiveSelector,
    private val props: InsightPolicyProperties,
) : InsightGenerator {
    override fun generate(ctx: InsightContext): GeneratedInsight? {
        if (!props.llmEnabled) return null

        val cleanedCandidates = ctx.logs.map { sanitizer.sanitize(it.content) }

        val selected =
            selector.select(
                candidates = cleanedCandidates,
                matchedKeyword = ctx.matchedKeyword,
                maxSelected = props.maxSelectedLogs.coerceIn(1, 40),
            )

        val merged = selected.joinToString("\n\n").trim()
        if (merged.length < props.minChars) return null

        // ✅ ko/en 뿐 아니라 ja/zh도 지원 (외부 의존성 없음)
        val outputLangCode = detectDominantLangCode(selected)

        val systemTemplate = promptLoader.loadSystemPrompt()
        val userTemplate = promptLoader.loadUserTemplate()
        val schemaJson = promptLoader.loadSchemaJson()

        val kindHint =
            perspectiveSelector.chooseKindHint(
                selectedLogs = selected,
                matchedKeyword = ctx.matchedKeyword,
            )

        val system = systemTemplate.replace("{{OUTPUT_LANG_CODE}}", outputLangCode)

        val userPrompt =
            buildUserPrompt(
                template = userTemplate,
                logs = selected,
                kindHint = kindHint.name,
                matchedKeyword = ctx.matchedKeyword,
                outputLangCode = outputLangCode,
            )

        val json =
            openAiClient
                .structureWithSchema(system = system, user = userPrompt, schemaJson = schemaJson)
                .trim()

        val parsed = parser.parse(json) ?: return null

        val normalizedKind =
            perspectiveSelector.normalizeKind(
                modelKind = parsed.kind,
                selectedLogs = selected,
                matchedKeyword = ctx.matchedKeyword,
            )

        return parsed.copy(kind = normalizedKind)
    }

    private fun buildUserPrompt(
        template: String,
        logs: List<String>,
        kindHint: String,
        matchedKeyword: String?,
        outputLangCode: String,
    ): String {
        val logsJoined =
            logs
                .mapIndexed { idx, s ->
                    val n = idx + 1
                    "$n) $s"
                }.joinToString("\n")

        return template
            .replace("{{KIND_HINT}}", kindHint)
            .replace("{{OUTPUT_LANG_CODE}}", outputLangCode)
            .replace("{{MATCHED_KEYWORD}}", matchedKeyword ?: "")
            .replace("{{LOGS}}", logsJoined)
    }

    /**
     * Dominant-language heuristic based on Unicode scripts.
     *
     * Returns:
     * - "ko" : Hangul dominant
     * - "ja" : Japanese (Kana) dominant
     * - "zh" : CJK ideographs dominant (Chinese-like; can also appear in Japanese)
     * - "en" : Latin letters dominant
     * - "und": unknown / not enough signal
     *
     * Notes:
     * - Japanese text often contains both Kana + Kanji.
     *   So: if Kana exists meaningfully, prefer "ja".
     * - Chinese is mostly CJK ideographs without Kana/Hangul.
     */
    private fun detectDominantLangCode(texts: List<String>): String {
        var hangul = 0
        var kana = 0
        var cjk = 0
        var latin = 0

        for (t in texts) {
            for (ch in t) {
                when {
                    isHangul(ch) -> hangul++
                    isKana(ch) -> kana++
                    isCjkIdeograph(ch) -> cjk++
                    isLatinLetter(ch) -> latin++
                }
            }
        }

        val totalSignal = hangul + kana + cjk + latin
        if (totalSignal < 12) return "und"

        // Japanese preference: if Kana exists at a non-trivial level, treat as ja
        // (Kana는 일본어에서 매우 강한 시그널)
        if (kana >= 6 && kana >= hangul && kana >= latin) return "ja"

        // Korean
        if (hangul >= latin && hangul >= cjk) return "ko"

        // Chinese-like (CJK ideographs) without Kana/Hangul dominance
        if (cjk >= latin && cjk >= hangul) return "zh"

        // Default Latin
        return "en"
    }

    private fun isLatinLetter(ch: Char): Boolean = (ch in 'A'..'Z') || (ch in 'a'..'z')

    private fun isHangul(ch: Char): Boolean =
        (ch in '\uAC00'..'\uD7A3') ||
            // Hangul syllables
            (ch in '\u1100'..'\u11FF') ||
            // Hangul Jamo
            (ch in '\u3130'..'\u318F') // Hangul Compatibility Jamo

    private fun isKana(ch: Char): Boolean =
        (ch in '\u3040'..'\u309F') ||
            // Hiragana
            (ch in '\u30A0'..'\u30FF') ||
            // Katakana
            (ch in '\u31F0'..'\u31FF') // Katakana Phonetic Extensions

    private fun isCjkIdeograph(ch: Char): Boolean =
        (ch in '\u4E00'..'\u9FFF') ||
            // CJK Unified Ideographs
            (ch in '\u3400'..'\u4DBF') // CJK Unified Ideographs Extension A
}
