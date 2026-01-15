package com.example.lifelog.domain.push

/**
 * 푸시 설정 도메인 리포지토리 인터페이스
 * 구현체는 infrastructure 레이어에 위치
 */
interface PushSettingRepository {
    fun findById(userId: Long): PushSetting?

    fun save(setting: PushSetting): PushSetting
}
