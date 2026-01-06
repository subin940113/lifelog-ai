package com.example.lifelog.interest

import org.springframework.data.jpa.repository.JpaRepository

interface InterestSettingsRepository : JpaRepository<InterestSettings, Long> {
    fun findByUserId(userId: Long): InterestSettings?
}
