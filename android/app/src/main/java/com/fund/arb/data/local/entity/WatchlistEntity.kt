package com.fund.arb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val code: String,
    val name: String,
    val addedAt: Long = System.currentTimeMillis()
)
