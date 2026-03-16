package com.fund.arb.data.remote.api

import com.fund.arb.data.remote.model.*
import retrofit2.Response
import retrofit2.http.*

interface EastmoneyApi {
    @GET("https://fund.eastmoney.com/data/rankhandler.aspx")
    suspend fun getLOFList(
        @Query("op") op: String = "ph",
        @Query("dt") dt: String = "kf",
        @Query("ft") ft: String = "lof",
        @Query("rs") rs: String = "",
        @Query("gs") gs: String = "0",
        @Query("sc") sc: String = "dm",
        @Query("st") st: String = "desc",
        @Query("qdii") qdii: String = "",
        @Query("tabSubtype") tabSubtype: String = ",,,,,",
        @Query("pi") pi: Int = 1,
        @Query("pn") pn: Int = 50,
        @Query("dx") dx: Int = 1
    ): Response<String>

    @GET("https://fund.eastmoney.com/tzzs/api_fundrules.ashx")
    suspend fun getPurchaseStatus(
        @Query("fc") fundCode: String,
        @Query("token") token: String = "fundeastmoney"
    ): Response<PurchaseResponse>

    @GET("https://fund.eastmoney.com/f10/F10Data.aspx")
    suspend fun getNavHistory(
        @Query("code") code: String,
        @Query("year") year: Int,
        @Query("sdate") sdate: String = "",
        @Query("edate") edate: String = "",
        @Query("per") per: Int = 30
    ): Response<String>
}
