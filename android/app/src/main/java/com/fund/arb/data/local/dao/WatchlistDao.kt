package com.fund.arb.data.local.dao

import androidx.room.*
import com.fund.arb.data.local.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    fun getAll(): Flow<List<WatchlistEntity>>

    @Query("SELECT * FROM watchlist WHERE code = :code")
    suspend fun getByCode(code: String): WatchlistEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE code = :code)")
    suspend fun isWatched(code: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(item: WatchlistEntity)

    @Delete
    suspend fun remove(item: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE code = :code")
    suspend fun removeByCode(code: String)
}
