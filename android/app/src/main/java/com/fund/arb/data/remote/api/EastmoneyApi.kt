package com.fund.arb.data.remote.api

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
        @Query("pn") pn: Int = 100,
        @Query("dx") dx: Int = 1
    ): Response<String>
}