package com.example.lifelog.log.structured

interface CategoryCountProjection {
    val category: String
    val count: Long
}