package com.example.lifelog.infrastructure.persistence.push

import com.example.lifelog.domain.push.PushToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface JpaPushTokenRepository : JpaRepository<PushToken, Long> {
    fun findByUserIdAndToken(
        userId: Long,
        token: String,
    ): PushToken?

    fun findByUserIdAndEnabledTrue(userId: Long): List<PushToken>

    @Query("select t from PushToken t where t.userId = :userId and t.enabled = true")
    fun findEnabledByUserId(
        @Param("userId") userId: Long,
    ): List<PushToken>

    fun deleteByUserIdAndToken(
        userId: Long,
        token: String,
    ): Long

    @Query(
        """
        select distinct t.userId
        from PushToken t
        where t.enabled = true
        """,
    )
    fun findDistinctEnabledUserIds(): List<Long>

    fun findAllByUserIdAndEnabledTrue(userId: Long): List<PushToken>

    fun deleteByToken(token: String)
}
