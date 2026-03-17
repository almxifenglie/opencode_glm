package com.fund.arb.data.repository

import com.fund.arb.data.local.dao.*
import com.fund.arb.data.local.entity.*
import com.fund.arb.data.remote.api.*
import com.fund.arb.data.remote.model.QDIIResponse
import com.fund.arb.data.remote.model.QDIIItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

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

    private val jisiluApi: JisiluApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.jisilu.cn/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(JisiluApi::class.java)
    }
    
    private val eastmoneyApi: EastmoneyApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://fund.eastmoney.com/")
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(EastmoneyApi::class.java)
    }

    fun getAllFunds(): Flow<List<FundDataEntity>> = fundDataDao.getAllFunds()

    fun getFundsByType(type: String): Flow<List<FundDataEntity>> = fundDataDao.getFundsByType(type)

    fun getOpenPurchaseFunds(): Flow<List<FundDataEntity>> = fundDataDao.getOpenPurchaseFunds()

    suspend fun getFundByCode(code: String): FundDataEntity? = fundDataDao.getFundByCode(code)

    suspend fun refreshAllData(): Result<String> = withContext(Dispatchers.IO) {
        try {
            println("Starting refreshAllData...")
            val errors = mutableListOf<String>()
            val allFunds = mutableListOf<FundDataEntity>()
            
            try {
                val qdiiResult = fetchQDII()
                println("QDII fetched: ${qdiiResult.size}")
                allFunds.addAll(qdiiResult)
            } catch (e: Exception) {
                errors.add("QDII: ${e.message}")
                println("QDII fetch failed: ${e.message}")
            }
            
            try {
                val lofResult = fetchLOF()
                println("LOF fetched: ${lofResult.size}")
                allFunds.addAll(lofResult)
            } catch (e: Exception) {
                errors.add("LOF: ${e.message}")
                println("LOF fetch failed: ${e.message}")
            }
            
            println("Total funds to save: ${allFunds.size}")
            
            if (allFunds.isNotEmpty()) {
                fundDataDao.deleteAll()
                fundDataDao.insertFunds(allFunds)
                savePremiumHistory(allFunds)
            }
            
            if (errors.isNotEmpty() && allFunds.isEmpty()) {
                Result.failure(Exception("获取数据失败:\n${errors.joinToString("\n")}"))
            } else if (errors.isNotEmpty()) {
                Result.success("部分成功，${allFunds.size}条数据。错误: ${errors.joinToString(", ")}")
            } else {
                Result.success("成功获取 ${allFunds.size} 条数据")
            }
        } catch (e: Exception) {
            println("refreshAllData error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private suspend fun fetchQDII(): List<FundDataEntity> {
        println("Fetching QDII data from jisilu...")
        val response = jisiluApi.getQDIIList()
        
        if (!response.isSuccessful) {
            throw Exception("API失败: ${response.code()} ${response.message()}")
        }
        
        val body = response.body()
            ?: throw Exception("响应体为空")
        
        if (body.rows.isEmpty()) {
            throw Exception("数据为空")
        }
        
        println("QDII API success, found ${body.rows.size} items")
        
        val funds = body.rows.mapNotNull { item ->
            val code = item.fundId ?: return@mapNotNull null
            val name = item.fundNm ?: return@mapNotNull null
            
            FundDataEntity(
                code = code,
                name = name,
                type = "QDII",
                price = item.price,
                changePct = item.getChangePct(),
                premiumRate = item.getPremiumRate(),
                navT1 = item.nav,
                navEstimate = null,
                purchaseStatus = null,
                purchaseLimit = null,
                volume = item.fundVol,
                amount = item.fundAmt,
                source = "jisilu",
                updateTime = System.currentTimeMillis()
            )
        }
        
        println("Successfully parsed ${funds.size} QDII funds")
        funds
    }
    
    private suspend fun fetchLOF(): List<FundDataEntity> {
        println("Fetching LOF data from eastmoney...")
        val response = eastmoneyApi.getLOFList()
        
        if (!response.isSuccessful) {
            throw Exception("API失败: ${response.code()} ${response.message()}")
        }
        
        val body = response.body()
            ?: throw Exception("响应体为空")
        
        if (body.isEmpty()) {
            throw Exception("数据为空")
        }
        
        val funds = parseLOFResponse(body)
        println("Successfully parsed ${funds.size} LOF funds")
        funds
    }
    
    private fun parseLOFResponse(html: String): List<FundDataEntity> {
        val funds = mutableListOf<FundDataEntity>()
        
        val dataPattern = """datas:(\[.*?\])""".toRegex(RegexOption.DOT_MATCHES_ALL)
        val match = dataPattern.find(html)
            ?: throw Exception("无法解析数据格式")
        
        val dataArray = match.groupValues[1]
        val itemPattern = """"([^"]*)"""".toRegex()
        val items = itemPattern.findAll(dataArray).map { it.groupValues[1] }.toList()
        
        var i = 0
        while (i + 22 < items.size) {
            val code = items[i]
            val name = items[i + 1]
            
            if (code.length == 6 && code.all { it.isDigit() }) {
                funds.add(FundDataEntity(
                    code = code,
                    name = name,
                    type = "LOF",
                    price = items[i + 3].toDoubleOrNull(),
                    changePct = items[i + 4].replace("%", "").toDoubleOrNull(),
                    premiumRate = items[i + 15].replace("%", "").toDoubleOrNull(),
                    navT1 = items[i + 6].toDoubleOrNull(),
                    navEstimate = items[i + 17].toDoubleOrNull(),
                    purchaseStatus = null,
                    purchaseLimit = null,
                    volume = items[i + 9].toDoubleOrNull(),
                    amount = items[i + 10].toDoubleOrNull(),
                    source = "eastmoney",
                    updateTime = System.currentTimeMillis()
                ))
            }
            i += 23
        }
        
        funds
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