package com.fund.arb.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fund.arb.data.local.dao.*
import com.fund.arb.data.local.entity.*

@Database(
    entities = [
        FundDataEntity::class,
        PremiumHistoryEntity::class,
        NavHistoryEntity::class,
        WatchlistEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fundDataDao(): FundDataDao
    abstract fun premiumHistoryDao(): PremiumHistoryDao
    abstract fun navHistoryDao(): NavHistoryDao
    abstract fun watchlistDao(): WatchlistDao
}
