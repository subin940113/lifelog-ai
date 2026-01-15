package com.example.lifelog.infrastructure.persistence.push

import com.example.lifelog.domain.push.PushSetting
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaPushSettingRepository : JpaRepository<PushSetting, Long> {
    override fun findById(userId: Long): java.util.Optional<PushSetting>
}
