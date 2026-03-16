package com.fund.arb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fund_data")
data class FundDataEntity(
    @PrimaryKey val code: String,
    val name: String,
    val type: String,
    val price: Double? = null,
    val changePct: Double? = null,
    val premiumRate: Double? = null,
    val navT1: Double? = null,
    val navEstimate: Double? = null,
    val purchaseStatus: String? = null,
    val purchaseLimit: Double? = null,
    val volume: Double? = null,
    val amount: Double? = null,
    val source: String? = null,
    val updateTime: Long = System.currentTimeMillis()
)
