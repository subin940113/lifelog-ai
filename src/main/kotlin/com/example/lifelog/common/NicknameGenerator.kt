package com.example.lifelog.common

/**
 * 랜덤 닉네임 생성기
 */
object NicknameGenerator {
    private val adjectives =
        listOf(
            "행복한",
            "즐거운",
            "용감한",
            "빛나는",
            "신비로운",
            "따뜻한",
            "영리한",
            "든든한",
            "상냥한",
            "똑똑한",
            "위대한",
            "우아한",
            "명랑한",
            "대담한",
            "차분한",
            "유쾌한",
            "열정적인",
            "침착한",
            "우직한",
            "발랄한",
            "세련된",
            "근사한",
            "소중한",
            "활기찬",
        )

    private val nouns =
        listOf(
            "다람쥐",
            "사자",
            "구름",
            "별빛",
            "바다",
            "호랑이",
            "여우",
            "고양이",
            "펭귄",
            "판다",
            "쿼카",
            "나무늘보",
            "돌고래",
            "토끼",
            "강아지",
            "코알라",
            "북극곰",
            "사슴",
            "너구리",
            "해바라기",
            "파랑새",
            "코끼리",
            "거북이",
            "기린",
        )

    fun generateRandomNickname(): String {
        val adjective = adjectives.random()
        val noun = nouns.random()

        return "$adjective$noun"
    }
}
