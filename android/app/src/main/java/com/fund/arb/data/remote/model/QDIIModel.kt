package com.fund.arb.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QDIIResponse(
    val rows: List<QDIIItem>
)

@JsonClass(generateAdapter = true)
data class QDIIItem(
    val fundId: String?,
    val fundNm: String?,
    val price: Double?,
    val increaseRt: String?,
    val premiumRt: String?,
    val nav: Double?,
    val fundVol: Double?,
    val fundAmt: Double?
) {
    fun getPremiumRate(): Double? {
        return premiumRt?.replace("%", "")?.toDoubleOrNull()
    }
    
    fun getChangePct(): Double? {
        return increaseRt?.replace("%", "")?.toDoubleOrNull()
    }
}
