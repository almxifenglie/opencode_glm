package com.fund.arb.data.repository

import com.fund.arb.data.local.dao.*
import com.fund.arb.data.local.entity.*
import com.fund.arb.data.remote.api.*
import com.fund.arb.data.remote.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class FundItem(
    val code: String,
    val name: String,
    val type: String,
    val price: Double?,
    val changePct: Double?,
    val premiumRate: Double?,
    val navT1: Double?,
    val navEstimate: Double?,
    val purchaseStatus: String?,
    val purchaseLimit: Double?,
    val volume: Double?,
    val amount: Double?,
    val source: String?,
    val updateTime: Long
)

@Singleton
class FundRepository @Inject constructor(
    private val fundDataDao: FundDataDao,
    private val premiumHistoryDao: PremiumHistoryDao,
    private val navHistoryDao: NavHistoryDao,
    private val watchlistDao: WatchlistDao
) {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // 后端API地址 - 可以修改为你的服务器地址
    private var backendUrl = "http://192.168.1.100:8000"  // 默认本地网络地址
    
    private val backendApi: BackendApi by lazy {
        Retrofit.Builder()
            .baseUrl(backendUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(BackendApi::class.java)
    }
    
    fun setBackendUrl(url: String) {
        backendUrl = url
    }

    fun getAllFunds(): Flow<List<FundDataEntity>> = fundDataDao.getAllFunds()

    fun getFundsByType(type: String): Flow<List<FundDataEntity>> = fundDataDao.getFundsByType(type)

    fun getOpenPurchaseFunds(): Flow<List<FundDataEntity>> = fundDataDao.getOpenPurchaseFunds()

    suspend fun getFundByCode(code: String): FundDataEntity? = fundDataDao.getFundByCode(code)

    suspend fun refreshAllData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            println("Starting refreshAllData...")
            val qdiiResult = fetchQDII()
            val lofResult = fetchLOF()
            
            println("QDII fetched: ${qdiiResult.size}, LOF fetched: ${lofResult.size}")
            
            val allFunds = mutableListOf<FundDataEntity>()
            allFunds.addAll(qdiiResult)
            allFunds.addAll(lofResult)
            
            println("Total funds to save: ${allFunds.size}")
            
            fundDataDao.deleteAll()
            fundDataDao.insertFunds(allFunds)
            
            savePremiumHistory(allFunds)
            
            println("refreshAllData completed successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            println("refreshAllData error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private suspend fun fetchQDII(): List<FundDataEntity> {
        return try {
            println("Fetching QDII data from backend: $backendUrl")
            val response = backendApi.getQDIIRanking()
            
            if (!response.isSuccessful) {
                println("QDII API failed: ${response.code()} - ${response.message()}")
                return getDemoData("QDII")  // 返回演示数据
            }
            
            val body = response.body()
            if (body == null || body.items.isEmpty()) {
                println("QDII API returned empty body, using demo data")
                return getDemoData("QDII")
            }
            
            println("QDII API success, found ${body.items.size} items")
            
            body.items.map { item ->
                FundDataEntity(
                    code = item.code,
                    name = item.name,
                    type = "QDII",
                    price = item.price,
                    changePct = item.changePct,
                    premiumRate = item.premiumRate,
                    navT1 = item.navT1,
                    navEstimate = item.navEstimate,
                    purchaseStatus = item.purchaseStatus,
                    purchaseLimit = item.purchaseLimit,
                    volume = item.volume,
                    amount = item.amount,
                    source = item.source ?: "backend",
                    updateTime = System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            println("fetchQDII error: ${e.message}")
            e.printStackTrace()
            getDemoData("QDII")  // 出错时返回演示数据
        }
    }
    
    private suspend fun fetchLOF(): List<FundDataEntity> {
        return try {
            println("Fetching LOF data from backend: $backendUrl")
            val response = backendApi.getLOFRanking()
            
            if (!response.isSuccessful) {
                println("LOF API failed: ${response.code()} - ${response.message()}")
                return getDemoData("LOF")  // 返回演示数据
            }
            
            val body = response.body()
            if (body == null || body.items.isEmpty()) {
                println("LOF API returned empty body, using demo data")
                return getDemoData("LOF")
            }
            
            println("LOF API success, found ${body.items.size} items")
            
            body.items.map { item ->
                FundDataEntity(
                    code = item.code,
                    name = item.name,
                    type = "LOF",
                    price = item.price,
                    changePct = item.changePct,
                    premiumRate = item.premiumRate,
                    navT1 = item.navT1,
                    navEstimate = item.navEstimate,
                    purchaseStatus = item.purchaseStatus,
                    purchaseLimit = item.purchaseLimit,
                    volume = item.volume,
                    amount = item.amount,
                    source = item.source ?: "backend",
                    updateTime = System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            println("fetchLOF error: ${e.message}")
            e.printStackTrace()
            getDemoData("LOF")  // 出错时返回演示数据
        }
    }
    
    private fun getDemoData(type: String): List<FundDataEntity> {
        // 返回演示数据用于测试
        println("Generating demo data for $type")
        return if (type == "QDII") {
            listOf(
                FundDataEntity(
                    code = "159941",
                    name = "纳指ETF",
                    type = "QDII",
                    price = 1.234,
                    changePct = 2.56,
                    premiumRate = 3.21,
                    navT1 = 1.195,
                    purchaseStatus = "开放",
                    purchaseLimit = 100.0,
                    source = "demo",
                    updateTime = System.currentTimeMillis()
                ),
                FundDataEntity(
                    code = "513100",
                    name = "纳指100ETF",
                    type = "QDII",
                    price = 1.567,
                    changePct = -1.23,
                    premiumRate = 2.45,
                    navT1 = 1.529,
                    purchaseStatus = "开放",
                    purchaseLimit = 50.0,
                    source = "demo",
                    updateTime = System.currentTimeMillis()
                ),
                FundDataEntity(
                    code = "159920",
                    name = "恒生ETF",
                    type = "QDII",
                    price = 0.987,
                    changePct = 0.85,
                    premiumRate = 1.67,
                    navT1 = 0.970,
                    purchaseStatus = "暂停",
                    purchaseLimit = null,
                    source = "demo",
                    updateTime = System.currentTimeMillis()
                )
            )
        } else {
            listOf(
                FundDataEntity(
                    code = "161725",
                    name = "招商白酒A",
                    type = "LOF",
                    price = 1.234,
                    changePct = 1.23,
                    premiumRate = 0.89,
                    navT1 = 1.223,
                    navEstimate = 1.229,
                    purchaseStatus = "开放",
                    purchaseLimit = 10.0,
                    source = "demo",
                    updateTime = System.currentTimeMillis()
                ),
                FundDataEntity(
                    code = "163406",
                    name = "兴全合润",
                    type = "LOF",
                    price = 2.345,
                    changePct = -0.56,
                    premiumRate = -0.34,
                    navT1 = 2.353,
                    navEstimate = 2.351,
                    purchaseStatus = "开放",
                    purchaseLimit = null,
                    source = "demo",
                    updateTime = System.currentTimeMillis()
                )
            )
        }
    }

    private suspend fun savePremiumHistory(funds: List<FundDataEntity>) {
        val historyList = funds.map { fund ->
            PremiumHistoryEntity(
                code = fund.code,
                premiumRate = fund.premiumRate,
                nav = fund.navT1,
                price = fund.price,
                timestamp = System.currentTimeMillis()
            )
        }
        premiumHistoryDao.insertAll(historyList)
    }

    suspend fun refreshFund(code: String): Result<FundDataEntity?> = withContext(Dispatchers.IO) {
        try {
            val existingFund = fundDataDao.getFundByCode(code) ?: return@withContext Result.success(null)
            
            if (existingFund.type == "QDII") {
                val qdiiFunds = fetchQDII()
                val updatedFund = qdiiFunds.find { it.code == code }
                if (updatedFund != null) {
                    fundDataDao.updateFund(updatedFund)
                    premiumHistoryDao.insert(PremiumHistoryEntity(
                        code = updatedFund.code,
                        premiumRate = updatedFund.premiumRate,
                        nav = updatedFund.navT1,
                        price = updatedFund.price
                    ))
                    return@withContext Result.success(updatedFund)
                }
            } else {
                val lofFunds = fetchLOF()
                val updatedFund = lofFunds.find { it.code == code }
                if (updatedFund != null) {
                    fundDataDao.updateFund(updatedFund)
                    premiumHistoryDao.insert(PremiumHistoryEntity(
                        code = updatedFund.code,
                        premiumRate = updatedFund.premiumRate,
                        nav = updatedFund.navT1,
                        price = updatedFund.price
                    ))
                    return@withContext Result.success(updatedFund)
                }
            }
            
            Result.success(existingFund)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getPremiumHistory(code: String): Flow<List<PremiumHistoryEntity>> = 
        premiumHistoryDao.getHistoryByCode(code, 100)

    fun getNavHistory(code: String): Flow<List<NavHistoryEntity>> = 
        navHistoryDao.getHistoryByCode(code, 30)

    fun getWatchlist(): Flow<List<WatchlistEntity>> = watchlistDao.getAll()

    suspend fun addToWatchlist(code: String, name: String) {
        watchlistDao.add(WatchlistEntity(code, name))
    }

    suspend fun removeFromWatchlist(code: String) {
        watchlistDao.removeByCode(code)
    }

    suspend fun isInWatchlist(code: String): Boolean = watchlistDao.isWatched(code)

    suspend fun getLastUpdateTime(): Long? = fundDataDao.getLastUpdateTime()

    suspend fun getAllFundsSync(): List<FundDataEntity> = fundDataDao.getAllFundsSync()
}
