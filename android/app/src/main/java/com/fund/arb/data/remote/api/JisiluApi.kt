package com.fund.arb.data.remote.api

import com.fund.arb.data.remote.model.QDIIResponse
import retrofit2.Response
import retrofit2.http.*

interface JisiluApi {
    @FormUrlEncoded
    @POST("https://www.jisilu.cn/data/qdii/qdii_list/")
    suspend fun getQDIIList(
        @Field("fund_type") fundType: String = "QDII",
        @Field("rp") rp: Int = 50,
        @Field("page") page: Int = 1
    ): Response<QDIIResponse>
}
