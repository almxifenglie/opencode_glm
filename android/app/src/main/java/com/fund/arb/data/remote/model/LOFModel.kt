package com.fund.arb.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LOFResponse(
    @Json(name = "datas") val datas: List<List<String?>>?
)

@JsonClass(generateAdapter = true)
data class LOFItem(
    val code: String?,
    val name: String?,
    val price: Double?,
    val changePct: Double?,
    val nav: Double?,
    val navEstimate: Double?,
    val premiumRate: Double?,
    val volume: Double?,
    val amount: Double?
)
