package com.example.lifelog.push

import org.springframework.data.jpa.repository.JpaRepository

interface PushSettingRepository : JpaRepository<PushSetting, Long>
