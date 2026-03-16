package com.fund.arb.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QDIIResponse(
    @Json(name = "rows") val rows: List<QDIIItem>
)

@JsonClass(generateAdapter = true)
data class QDIIItem(
    @Json(name = "fund_id") val fundId: String?,
    @Json(name = "fund_nm") val fundNm: String?,
    @Json(name = "price") val price: Double?,
    @Json(name = "increase_rt") val increaseRt: String?,
    @Json(name = "premium_rt") val premiumRt: String?,
    @Json(name = "nav_dt") val navDt: String?,
    @Json(name = "nav") val nav: Double?,
    @Json(name = "fund_vol") val fundVol: Double?,
    @Json(name = "fund_amt") val fundAmt: Double?,
    @Json(name = "apply_fee") val applyFee: String?,
    @Json(name = "redeem_fee") val redeemFee: String?,
    @Json(name = "last_time") val lastTime: String?
) {
    fun getPremiumRate(): Double? {
        return premiumRt?.replace("%", "")?.toDoubleOrNull()
    }
    
    fun getChangePct(): Double? {
        return increaseRt?.replace("%", "")?.toDoubleOrNull()
    }
}
