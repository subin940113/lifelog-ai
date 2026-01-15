package com.example.lifelog.application.push

/**
 * 푸시 인텐트 데이터 키 상수
 */
object PushIntentDataKeys {
    const val INTENT_TYPE = "intent_type"
    const val KEYWORD = "keyword"
    const val INSIGHT_ID = "insight_id"
}

/**
 * 푸시 인텐트 타입 상수
 */
object PushIntentTypes {
    const val RECORD_PROMPT = "record_prompt"
    const val INSIGHT_DETAIL = "insight_detail"
}
