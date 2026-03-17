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
            
            if (allFunds.isNotEmpty()) {
                fundDataDao.deleteAll()
                fundDataDao.insertFunds(allFunds)
                savePremiumHistory(allFunds)
            }
            
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
            println("Fetching QDII data from jisilu...")
            val response = jisiluApi.getQDIIList()
            
            if (!response.isSuccessful) {
                println("QDII API failed: ${response.code()} - ${response.message()}")
                return getDemoData("QDII")
            }
            
            val body = response.body()
            if (body == null || body.rows.isEmpty()) {
                println("QDII API returned empty body, using demo data")
                return getDemoData("QDII")
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
            if (funds.isEmpty()) getDemoData("QDII") else funds
            
        } catch (e: Exception) {
            println("fetchQDII error: ${e.message}")
            e.printStackTrace()
            getDemoData("QDII")
        }
    }
    
    private suspend fun fetchLOF(): List<FundDataEntity> {
        return try {
            println("Fetching LOF data from eastmoney...")
            val response = eastmoneyApi.getLOFList()
            
            if (!response.isSuccessful) {
                println("LOF API failed: ${response.code()} - ${response.message()}")
                return getDemoData("LOF")
            }
            
            val body = response.body()
            if (body.isNullOrEmpty()) {
                println("LOF API returned empty body, using demo data")
                return getDemoData("LOF")
            }
            
            val funds = parseLOFResponse(body)
            println("Successfully parsed ${funds.size} LOF funds")
            
            if (funds.isEmpty()) getDemoData("LOF") else funds
            
        } catch (e: Exception) {
            println("fetchLOF error: ${e.message}")
            e.printStackTrace()
            getDemoData("LOF")
        }
    }
    
    private fun parseLOFResponse(html: String): List<FundDataEntity> {
        val funds = mutableListOf<FundDataEntity>()
        
        try {
            // 提取 datas 数组
            val dataPattern = """datas:(\[.*?\])""".toRegex(RegexOption.DOT_MATCHES_ALL)
            val match = dataPattern.find(html) ?: run {
                println("Could not find datas pattern in LOF response")
                return funds
            }
            
            val dataArray = match.groupValues[1]
            
            // 解析数组中的每一项
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
        } catch (e: Exception) {
            println("parseLOFResponse error: ${e.message}")
        }
        
        return funds
    }
    
    private fun getDemoData(type: String): List<FundDataEntity> {
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