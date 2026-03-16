package com.fund.arb.data.local.dao

import androidx.room.*
import com.fund.arb.data.local.entity.PremiumHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PremiumHistoryDao {
    @Query("SELECT * FROM premium_history WHERE code = :code ORDER BY timestamp DESC LIMIT :limit")
    fun getHistoryByCode(code: String, limit: Int = 100): Flow<List<PremiumHistoryEntity>>

    @Query("SELECT * FROM premium_history WHERE code = :code AND timestamp >= :startTime ORDER BY timestamp DESC")
    fun getHistorySince(code: String, startTime: Long): Flow<List<PremiumHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: PremiumHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(historyList: List<PremiumHistoryEntity>)

    @Query("DELETE FROM premium_history WHERE timestamp < :beforeTime")
    suspend fun deleteOldRecords(beforeTime: Long)

    @Query("DELETE FROM premium_history WHERE code = :code")
    suspend fun deleteByCode(code: String)
}
