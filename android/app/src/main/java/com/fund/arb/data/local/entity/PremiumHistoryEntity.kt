package com.fund.arb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "premium_history")
data class PremiumHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val premiumRate: Double? = null,
    val nav: Double? = null,
    val price: Double? = null,
    val timestamp: Long = System.currentTimeMillis()
)
