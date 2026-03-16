package com.fund.arb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nav_history")
data class NavHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val navDate: String,
    val nav: Double? = null,
    val accNav: Double? = null,
    val changePct: Double? = null,
    val source: String = "eastmoney",
    val createdAt: Long = System.currentTimeMillis()
)
