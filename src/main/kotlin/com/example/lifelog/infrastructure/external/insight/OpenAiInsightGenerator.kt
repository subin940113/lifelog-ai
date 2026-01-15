package com.example.lifelog.infrastructure.external.insight

import com.example.lifelog.application.insight.pipeline.InsightGenerator
import com.example.lifelog.domain.insight.GeneratedInsight
import com.example.lifelog.domain.insight.InsightContext
import com.example.lifelog.domain.insight.RecentInsight
import com.example.lifelog.infrastructure.config.InsightPolicyProperties
import com.example.lifelog.infrastructure.external.openai.OpenAiClient
import org.springframework.stereotype.Component
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * OpenAI 기반 인사이트 생성기
 */
@Component
class OpenAiInsightGenerator(
    private val openAiClient: OpenAiClient,
    private val promptLoader: InsightPromptLoader,
    private val sanitizer: LlmSanitizer,
    private val parser: InsightLlmParser,
    private val selector: InsightLogSelector,
    private val perspectiveSelector: InsightPerspectiveSelector,
    private val properties: InsightPolicyProperties,
) : InsightGenerator {
    override fun generate(ctx: InsightContext): GeneratedInsight? {
        if (!properties.llmEnabled) return null

        val candidates =
            ctx.logs.map { log ->
                InsightLogSelector.LogCandidate(
                    content = sanitizer.sanitize(log.content),
                    createdAt = log.createdAt,
                )
            }

        val selected =
            selector.select(
                candidates = candidates,
                matchedKeyword = ctx.matchedKeyword,
                maxSelected = properties.maxSelectedLogs.coerceIn(1, 40),
            )

        val merged = selected.joinToString("\n\n").trim()
        if (merged.length < properties.minChars) return null

        // 1) 언어 결정 (und면 default)
        val detected =
            detectDominantLangCode(selected).let { code ->
                if (code == "und") properties.defaultLangCode else code
            }

        // 2) 언어 스펙(지시문/허용 스크립트)으로 변환
        val langSpec = resolveLangSpec(detected, properties.defaultLangCode)

        val systemTemplate = promptLoader.loadSystemPrompt()
        val userTemplate = promptLoader.loadUserTemplate()
        val schemaJson = promptLoader.loadSchemaJson()

        val kindHint =
            perspectiveSelector.chooseKindHint(
                userId = ctx.userId,
                selectedLogs = selected,
                matchedKeyword = ctx.matchedKeyword,
            )

        // 시스템에는 "언어코드"가 아니라 "명시적 지시문"을 주입
        val system = systemTemplate.replace("{{OUTPUT_LANG_CODE}}", langSpec.instruction)

        val userPrompt =
            buildUserPrompt(
                template = userTemplate,
                logs = selected,
                recentInsights = ctx.recentInsights,
                kindHint = kindHint.name,
                matchedKeyword = ctx.matchedKeyword,
                outputLangInstruction = langSpec.instruction,
            )

        // 3) 1차 호출
        val json1 =
            openAiClient
                .structureWithSchema(system = system, user = userPrompt, schemaJson = schemaJson)
                .trim()

        val parsed1 = parser.parse(json1) ?: return null

        // 4) 언어 검증 + 필요 시 1회 재시도
        val parsedFinal =
            if (languageOkForFields(langSpec, parsed1)) {
                parsed1
            } else {
                val system2 =
                    system +
                        "\nCRITICAL: title, body, evidence, keyword MUST be written in ${langSpec.instruction}. " +
                        "Never mix languages. If you cannot comply, set should_generate=false and null fields."

                val json2 =
                    openAiClient
                        .structureWithSchema(system = system2, user = userPrompt, schemaJson = schemaJson)
                        .trim()

                val parsed2 = parser.parse(json2) ?: return null

                if (languageOkForFields(langSpec, parsed2)) parsed2 else parsed1
                // 재시도도 실패하면 parsed1을 유지(또는 null 처리로 바꿔도 됩니다)
            }

        val normalizedKind =
            perspectiveSelector.normalizeKind(
                userId = ctx.userId,
                modelKind = parsedFinal.kind,
                selectedLogs = selected,
                matchedKeyword = ctx.matchedKeyword,
            )

        val finalKeyword = parsedFinal.keyword ?: ctx.matchedKeyword
        return parsedFinal.copy(kind = normalizedKind, keyword = finalKeyword)
    }

    private fun buildUserPrompt(
        template: String,
        logs: List<String>,
        recentInsights: List<RecentInsight>,
        kindHint: String,
        matchedKeyword: String?,
        outputLangInstruction: String,
    ): String {
        val logsJoined =
            logs.mapIndexed { idx, s -> "${idx + 1}) $s" }.joinToString("\n")

        val recentInsightsBlock = formatRecentInsights(recentInsights)

        return template
            .replace("{{KIND_HINT}}", kindHint)
            .replace("{{OUTPUT_LANG_CODE}}", outputLangInstruction)
            .replace("{{MATCHED_KEYWORD}}", matchedKeyword ?: "")
            .replace("{{RECENT_INSIGHTS}}", recentInsightsBlock)
            .replace("{{LOGS}}", logsJoined)
    }

    private fun formatRecentInsights(recent: List<RecentInsight>): String {
        if (recent.isEmpty()) return "None"

        val tz = ZoneId.of("Asia/Seoul")
        val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        return recent
            .sortedByDescending { it.createdAt }
            .take(properties.maxRecentInsights.coerceIn(1, 10))
            .mapIndexed { idx, it ->
                val n = idx + 1
                val created = it.createdAt.atZone(tz).format(fmt)
                val title =
                    it.title
                        .replace("\n", " ")
                        .trim()
                        .take(80)
                val body =
                    it.body
                        .replace("\n", " ")
                        .trim()
                        .take(220)
                "$n) [$created] [${it.kind}] $title — $body"
            }.joinToString("\n")
    }

    // ---------------------------
    // Language enforcement (dynamic)
    // ---------------------------

    private enum class Script { HANGUL, LATIN, KANA, CJK }

    private data class LangSpec(
        val code: String,
        val instruction: String,
        val allowedScripts: Set<Script>,
        val requiredAnyOf: Set<Script>,
    )

    private fun resolveLangSpec(
        codeRaw: String,
        defaultCode: String,
    ): LangSpec {
        val code = codeRaw.lowercase().ifBlank { defaultCode.lowercase() }

        return when (code) {
            "ko", "ko-kr" ->
                LangSpec(
                    code = "ko",
                    instruction = "Korean (Hangul) ONLY",
                    allowedScripts = setOf(Script.HANGUL),
                    requiredAnyOf = setOf(Script.HANGUL),
                )
            "en", "en-us", "en-gb" ->
                LangSpec(
                    code = "en",
                    instruction = "English ONLY",
                    allowedScripts = setOf(Script.LATIN),
                    requiredAnyOf = setOf(Script.LATIN),
                )
            "ja", "ja-jp" ->
                // 일본어는 가나 + 한자(CJK) 혼용이 정상
                LangSpec(
                    code = "ja",
                    instruction = "Japanese ONLY",
                    allowedScripts = setOf(Script.KANA, Script.CJK),
                    requiredAnyOf = setOf(Script.KANA, Script.CJK),
                )
            "zh", "zh-cn", "zh-tw" ->
                LangSpec(
                    code = "zh",
                    instruction = "Chinese ONLY",
                    allowedScripts = setOf(Script.CJK),
                    requiredAnyOf = setOf(Script.CJK),
                )
            else -> resolveLangSpec(defaultCode, defaultCode)
        }
    }

    private fun languageOkForFields(
        spec: LangSpec,
        parsed: GeneratedInsight,
    ): Boolean {
        val merged =
            listOf(parsed.title, parsed.body, parsed.evidence, parsed.keyword)
                .filterNotNull()
                .joinToString(" ")
                .trim()

        if (merged.isBlank()) return true

        val scripts = detectScripts(merged)

        // requiredAnyOf 중 하나는 반드시 포함
        if (scripts.intersect(spec.requiredAnyOf).isEmpty()) return false

        // allowedScripts 밖이 섞이면 실패
        if (!spec.allowedScripts.containsAll(scripts)) return false

        return true
    }

    private fun detectScripts(text: String): Set<Script> {
        val s = mutableSetOf<Script>()
        if (text.any { isHangul(it) }) s += Script.HANGUL
        if (text.any { isLatinLetter(it) }) s += Script.LATIN
        if (text.any { isKana(it) }) s += Script.KANA
        if (text.any { isCjkIdeograph(it) }) s += Script.CJK
        return s
    }

    // ---------------------------
    // Dominant-language heuristic (existing)
    // ---------------------------

    /**
     * Dominant-language heuristic based on Unicode scripts.
     *
     * Returns:
     * - "ko" : Hangul dominant
     * - "ja" : Japanese (Kana) dominant
     * - "zh" : CJK ideographs dominant
     * - "en" : Latin letters dominant
     * - "und": unknown / not enough signal
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

        if (kana >= 6 && kana >= hangul && kana >= latin) return "ja"
        if (hangul >= latin && hangul >= cjk) return "ko"
        if (cjk >= latin && cjk >= hangul) return "zh"
        return "en"
    }

    private fun isLatinLetter(ch: Char): Boolean = (ch in 'A'..'Z') || (ch in 'a'..'z')

    private fun isHangul(ch: Char): Boolean =
        (ch in '\uAC00'..'\uD7A3') ||
            (ch in '\u1100'..'\u11FF') ||
            (ch in '\u3130'..'\u318F')

    private fun isKana(ch: Char): Boolean =
        (ch in '\u3040'..'\u309F') ||
            (ch in '\u30A0'..'\u30FF') ||
            (ch in '\u31F0'..'\u31FF')

    private fun isCjkIdeograph(ch: Char): Boolean =
        (ch in '\u4E00'..'\u9FFF') ||
            (ch in '\u3400'..'\u4DBF')
}
