package com.fund.arb.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QDIIResponse(
    val rows: List<QDIIItem>
)

@JsonClass(generateAdapter = true)
data class QDIIItem(
    val cell: QDIICell
) {
    val fundId: String? get() = cell?.fundId
    val fundNm: String? get() = cell?.fundNm
    val price: Double? get() = cell?.price
    val nav: Double? get() = cell?.fundNav
    val fundVol: Double? get() = cell?.volume?.toDoubleOrNull()
    val fundAmt: Double? get() = cell?.amount?.toDouble()
    
    fun getChangePct(): Double? {
        return cell?.increaseRt?.replace("%", "")?.toDoubleOrNull()
    }
    
    fun getPremiumRate(): Double? {
        return cell?.navDiscountRt
    }
}

@JsonClass(generateAdapter = true)
data class QDIICell(
    @Json(name = "fund_id") val fundId: String?,
    @Json(name = "fund_nm") val fundNm: String?,
    val price: Double?,
    @Json(name = "increase_rt") val increaseRt: String?,
    @Json(name = "nav_discount_rt") val navDiscountRt: Double?,
    @Json(name = "fund_nav") val fundNav: Double?,
    val volume: String?,
    val amount: Long?
)