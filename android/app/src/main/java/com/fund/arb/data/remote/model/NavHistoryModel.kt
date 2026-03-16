package com.fund.arb.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NavHistoryResponse(
    @Json(name = "datas") val datas: List<NavHistoryItem>?
)

@JsonClass(generateAdapter = true)
data class NavHistoryItem(
    @Json(name = "fbrq") val date: String?,
    @Json(name = "dwjz") val nav: String?,
    @Json(name = "ljjz") val accNav: String?,
    @Json(name = "jzzzl") val changePct: String?
)
