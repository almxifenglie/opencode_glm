package com.fund.arb.ui.watchlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fund.arb.ui.components.FundCard
import com.fund.arb.ui.theme.Primary
import com.fund.arb.ui.theme.TextSecondary
import com.fund.arb.viewmodel.WatchlistViewModel

@Composable
fun WatchlistScreen(
    onFundClick: (String) -> Unit,
    viewModel: WatchlistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    when {
        uiState.funds.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("暂无自选基金", color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("在基金详情页点击收藏添加", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
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
                        onClick = { onFundClick(fund.code) }
                    )
                }
            }
        }
    }
}
