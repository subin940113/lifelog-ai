package com.example.lifelog.log.raw

import org.springframework.data.jpa.repository.JpaRepository

interface RawLogRepository : JpaRepository<RawLog, Long>