package com.fund.arb.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PurchaseResponse(
    @Json(name = "datas") val datas: List<PurchaseData>?
)

@JsonClass(generateAdapter = true)
data class PurchaseData(
    @Json(name = "fundcode") val fundCode: String?,
    @Json(name = "fundname") val fundName: String?,
    @Json(name = "sgzt") val purchaseStatus: String?,
    @Json(name = "shzt") val redeemStatus: String?,
    @Json(name = "sgcode") val sgCode: String?,
    @Json(name = "maxsg") val maxPurchase: String?
)

@JsonClass(generateAdapter = true)
data class FundInfoResponse(
    @Json(name = "datas") val datas: List<FundInfoData>?
)

@JsonClass(generateAdapter = true)
data class FundInfoData(
    @Json(name = "fundcode") val fundCode: String?,
    @Json(name = "fundname") val fundName: String?,
    @Json(name = "fundtype") val fundType: String?,
    @Json(name = "fundscale") val fundScale: String?
)
