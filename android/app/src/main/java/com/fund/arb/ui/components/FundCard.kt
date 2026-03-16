package com.fund.arb.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fund.arb.data.local.entity.FundDataEntity
import com.fund.arb.ui.theme.*

@Composable
fun FundCard(
    fund: FundDataEntity,
    isRefreshing: Boolean = false,
    onClick: () -> Unit,
    onRefresh: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fund.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${fund.code} · ${fund.type}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                PremiumBadge(premiumRate = fund.premiumRate)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FundInfoItem(label = "现价", value = fund.price?.let { "%.3f".format(it) } ?: "-")
                FundInfoItem(
                    label = "涨跌",
                    value = fund.changePct?.let { "%+.2f%%".format(it) } ?: "-",
                    color = when {
                        fund.changePct != null && fund.changePct > 0 -> Positive
                        fund.changePct != null && fund.changePct < 0 -> Negative
                        else -> TextSecondary
                    }
                )
                FundInfoItem(
                    label = "净值",
                    value = fund.navT1?.let { "%.4f".format(it) } ?: fund.navEstimate?.let { "%.4f".format(it) } ?: "-"
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    fund.purchaseStatus?.let { status ->
                        PurchaseTag(isOpen = status == "开放")
                    }
                    fund.purchaseLimit?.let { limit ->
                        Text(
                            text = "限额 ${limit}万",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                
                Button(
                    onClick = onRefresh,
                    enabled = !isRefreshing,
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        disabledContainerColor = Primary.copy(alpha = 0.5f)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(
                        text = if (isRefreshing) "刷新中..." else "刷新",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumBadge(premiumRate: Double?) {
    val isPositive = (premiumRate ?: 0.0) >= 0
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = when {
            premiumRate == null -> TextSecondary
            isPositive -> Positive
            else -> Negative
        }
    ) {
        Text(
            text = premiumRate?.let { "%+.2f%%".format(it) } ?: "-%",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            color = CardBackground,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun FundInfoItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color = TextPrimary
) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = "$label: $value",
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

@Composable
fun PurchaseTag(isOpen: Boolean) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = if (isOpen) Positive else Negative
    ) {
        Text(
            text = if (isOpen) "可申购" else "暂停申购",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = CardBackground
        )
    }
}
