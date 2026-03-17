package com.fund.arb.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RankingResponse(
    @Json(name = "total") val total: Int,
    @Json(name = "items") val items: List<RankingItem>,
    @Json(name = "source") val source: String,
    @Json(name = "update_time") val updateTime: String? = null,
    @Json(name = "page") val page: Int = 1,
    @Json(name = "page_size") val pageSize: Int = 20,
    @Json(name = "total_pages") val totalPages: Int = 1
)

@JsonClass(generateAdapter = true)
data class RankingItem(
    @Json(name = "code") val code: String,
    @Json(name = "name") val name: String,
    @Json(name = "price") val price: Double?,
    @Json(name = "change_pct") val changePct: Double?,
    @Json(name = "premium_rate") val premiumRate: Double?,
    @Json(name = "nav_t1") val navT1: Double?,
    @Json(name = "nav_estimate") val navEstimate: Double?,
    @Json(name = "purchase_status") val purchaseStatus: String?,
    @Json(name = "purchase_limit") val purchaseLimit: Double?,
    @Json(name = "volume") val volume: Double?,
    @Json(name = "amount") val amount: Double?,
    @Json(name = "source") val source: String?,
    @Json(name = "update_time") val updateTime: String? = null
)