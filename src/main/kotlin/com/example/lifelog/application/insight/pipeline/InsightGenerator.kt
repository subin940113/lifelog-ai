package com.example.lifelog.application.insight.pipeline

import com.example.lifelog.domain.insight.GeneratedInsight
import com.example.lifelog.domain.insight.InsightContext

/**
 * 인사이트 생성기 인터페이스
 */
interface InsightGenerator {
    fun generate(ctx: InsightContext): GeneratedInsight?
}
