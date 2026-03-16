package com.fund.arb.ui.lof

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
import com.fund.arb.viewmodel.LOFViewModel

@Composable
fun LOFScreen(
    onFundClick: (String) -> Unit,
    viewModel: LOFViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
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
                Text("暂无数据", color = TextSecondary)
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
