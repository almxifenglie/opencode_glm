package com.fund.arb.data.local.dao

import androidx.room.*
import com.fund.arb.data.local.entity.FundDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FundDataDao {
    @Query("SELECT * FROM fund_data ORDER BY premiumRate DESC")
    fun getAllFunds(): Flow<List<FundDataEntity>>

    @Query("SELECT * FROM fund_data WHERE type = :fundType ORDER BY premiumRate DESC")
    fun getFundsByType(fundType: String): Flow<List<FundDataEntity>>

    @Query("SELECT * FROM fund_data WHERE premiumRate >= :minPremium ORDER BY premiumRate DESC")
    fun getFundsByMinPremium(minPremium: Double): Flow<List<FundDataEntity>>

    @Query("SELECT * FROM fund_data WHERE code = :code")
    suspend fun getFundByCode(code: String): FundDataEntity?

    @Query("SELECT * FROM fund_data WHERE purchaseStatus = '开放' ORDER BY premiumRate DESC")
    fun getOpenPurchaseFunds(): Flow<List<FundDataEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFund(fund: FundDataEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFunds(funds: List<FundDataEntity>)

    @Update
    suspend fun updateFund(fund: FundDataEntity)

    @Query("DELETE FROM fund_data")
    suspend fun deleteAll()

    @Query("SELECT MAX(updateTime) FROM fund_data")
    suspend fun getLastUpdateTime(): Long?
}
