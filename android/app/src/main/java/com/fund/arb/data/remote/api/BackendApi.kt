package com.fund.arb.data.remote.api

import com.fund.arb.data.remote.model.RankingResponse
import retrofit2.Response
import retrofit2.http.*

interface BackendApi {
    @GET("/api/all/ranking")
    suspend fun getAllRanking(
        @Query("min_premium") minPremium: Double = 0.0,
        @Query("purchase_open") purchaseOpen: Boolean = true,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 50
    ): Response<RankingResponse>
    
    @GET("/api/qdii/ranking")
    suspend fun getQDIIRanking(
        @Query("min_premium") minPremium: Double = 0.0,
        @Query("purchase_open") purchaseOpen: Boolean = true,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 50
    ): Response<RankingResponse>
    
    @GET("/api/lof/ranking")
    suspend fun getLOFRanking(
        @Query("min_premium") minPremium: Double = -50.0,
        @Query("purchase_open") purchaseOpen: Boolean = true,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 50
    ): Response<RankingResponse>
}