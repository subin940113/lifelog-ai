package com.example.lifelog.push

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "lifelog.push.policy")
data class PushPolicyProperties(
    val enabled: Boolean = true,
    val zone: String = "Asia/Seoul",
    val scanIntervalMinutes: Int = 10,
    val timePatternMiss: TimePatternMiss = TimePatternMiss(),
    val keywordNudge: KeywordNudge = KeywordNudge(),
    val insightCreated: InsightCreatedPolicy = InsightCreatedPolicy(),
    val message: Message = Message(),
) {
    data class TimePatternMiss(
        val enabled: Boolean = true,
        val lookbackDays: Int = 14,
        val minActiveDays: Int = 5,
        val bucketMinutes: Int = 60,
        val triggerDelayMinutes: Int = 30,
        val maxPerDay: Int = 1,
    )

    data class KeywordNudge(
        val enabled: Boolean = true,
        val cooldownDays: Int = 2,
        val maxPerDay: Int = 1,
        val lookbackDays: Int = 21, // 최근 N일을 보고
        val bucketMinutes: Int = 30, // 30분 단위 버킷
        val minOccurrences: Int = 3, // 최소 N회 등장해야 "패턴" 인정
        val triggerDelayMinutes: Int = 20, // 해당 시간대가 지나고 N분 후에만 발송
    )

    data class InsightCreatedPolicy(
        val enabled: Boolean = true,
        val maxPerDay: Int = 5,
    )

    data class Message(
        val timePatternTitle: String = "오늘은 어땠어?",
        val timePatternBody: String = "평소 이 시간쯤 기록이 있었는데 오늘은 아직 없네. 한 줄만 남겨볼래?",
        val keywordTitle: String = "기록 한 줄 어때?",
        val keywordBodyTemplate: String = "\"%s\" 오늘은 어땠어?",
        val insightTitle: String = "새 인사이트가 도착했어요",
        val insightBodyTemplate: String = "%s",
    )
}
