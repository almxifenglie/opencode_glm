package com.fund.arb.data.remote.api

import retrofit2.Response
import retrofit2.http.*

interface JisiluApi {
    @FormUrlEncoded
    @POST("https://www.jisilu.cn/data/qdii/qdii_list/")
    suspend fun getQDIIList(
        @Field("fund_type") fundType: String = "QDII",
        @Field("rp") rp: Int = 100,
        @Field("page") page: Int = 1
    ): Response<JisiluQDIIResponse>
}

data class JisiluQDIIResponse(
    val rows: List<JisiluQDIIItem>
)

data class JisiluQDIIItem(
    val fund_id: String?,
    val fund_nm: String?,
    val price: Double?,
    val increase_rt: String?,
    val premium_rt: String?,
    val nav: Double?,
    val fund_vol: Double?,
    val fund_amt: Double?
) {
    fun getPremiumRate(): Double? = premium_rt?.replace("%", "")?.toDoubleOrNull()
    fun getChangePct(): Double? = increase_rt?.replace("%", "")?.toDoubleOrNull()
}