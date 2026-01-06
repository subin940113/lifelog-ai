package com.example.lifelog.insight.pipeline

interface InsightGenerator {
    fun generate(ctx: InsightContext): GeneratedInsight?
}
