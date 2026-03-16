package com.fund.arb.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fund.arb.data.local.entity.FundDataEntity
import com.fund.arb.ui.components.FundCard
import com.fund.arb.ui.theme.*
import com.fund.arb.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    onFundClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        FilterBar(
            minPremium = uiState.minPremium,
            onlyOpenPurchase = uiState.onlyOpenPurchase,
            onFilterChange = { min, open -> viewModel.setFilter(min, open) }
        )
        
        when {
            uiState.isLoading && uiState.funds.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            uiState.funds.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无数据，请下拉刷新",
                        color = TextSecondary
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.funds, key = { it.code }) { fund ->
                        FundCard(
                            fund = fund,
                            isRefreshing = uiState.refreshingCode == fund.code,
                            onClick = { onFundClick(fund.code) },
                            onRefresh = { viewModel.refreshFund(fund.code) }
                        )
                    }
                }
                
                uiState.lastUpdateTime?.let { time ->
                    UpdateTimeText(time)
                }
            }
        }
    }
}

@Composable
fun FilterBar(
    minPremium: Double,
    onlyOpenPurchase: Boolean,
    onFilterChange: (Double, Boolean) -> Unit
) {
    val filters = listOf(
        "全部" to 0.0,
        "溢价>1%" to 1.0,
        "溢价>2%" to 2.0,
        "溢价>3%" to 3.0,
        "溢价>5%" to 5.0
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        filters.forEach { (label, premium) ->
            FilterChip(
                selected = minPremium == premium,
                onClick = { onFilterChange(premium, onlyOpenPurchase) },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Primary,
                    selectedLabelColor = CardBackground
                )
            )
        }
    }
}

@Composable
fun UpdateTimeText(timestamp: Long) {
    val format = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    Text(
        text = "更新时间: ${format.format(Date(timestamp))}",
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary
    )
}
