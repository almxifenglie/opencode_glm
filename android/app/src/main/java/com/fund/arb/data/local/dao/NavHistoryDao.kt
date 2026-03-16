package com.fund.arb.data.local.dao

import androidx.room.*
import com.fund.arb.data.local.entity.NavHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NavHistoryDao {
    @Query("SELECT * FROM nav_history WHERE code = :code ORDER BY navDate DESC LIMIT :limit")
    fun getHistoryByCode(code: String, limit: Int = 30): Flow<List<NavHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: NavHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(historyList: List<NavHistoryEntity>)

    @Query("DELETE FROM nav_history WHERE code = :code")
    suspend fun deleteByCode(code: String)

    @Query("DELETE FROM nav_history WHERE createdAt < :beforeTime")
    suspend fun deleteOldRecords(beforeTime: Long)
}
