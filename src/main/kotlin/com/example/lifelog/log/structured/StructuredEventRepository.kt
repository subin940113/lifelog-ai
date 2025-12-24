package com.example.lifelog.log.structured

import org.springframework.data.jpa.repository.JpaRepository

interface StructuredEventRepository : JpaRepository<StructuredEvent, Long>