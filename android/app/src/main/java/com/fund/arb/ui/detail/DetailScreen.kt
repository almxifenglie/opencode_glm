package com.fund.arb.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fund.arb.ui.theme.*
import com.fund.arb.viewmodel.DetailViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    code: String,
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    LaunchedEffect(code) {
        viewModel.loadFund(code)
    }
    
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.fund?.name ?: "基金详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.toggleWatchlist() }
                    ) {
                        Text(
                            text = if (uiState.isWatched) "已收藏" else "收藏",
                            color = if (uiState.isWatched) Primary else Primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = CardBackground,
                    navigationIconContentColor = CardBackground,
                    actionIconContentColor = CardBackground
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            uiState.fund?.let { fund ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PremiumCard(fund.premiumRate)
                    
                    InfoCard("基本信息") {
                        DetailRow("基金代码", fund.code)
                        DetailRow("基金名称", fund.name)
                        DetailRow("基金类型", fund.type)
                    }
                    
                    InfoCard("行情信息") {
                        DetailRow("最新价", fund.price?.let { "%.3f".format(it) } ?: "-")
                        DetailRow("涨跌幅", fund.changePct?.let { "%+.2f%%".format(it) } ?: "-")
                        DetailRow("净值", fund.navT1?.let { "%.4f".format(it) } ?: "-")
                        fund.navEstimate?.let {
                            DetailRow("估算净值", "%.4f".format(it))
                        }
                    }
                    
                    InfoCard("申购信息") {
                        DetailRow("申购状态", fund.purchaseStatus ?: "-")
                        DetailRow("申购限额", fund.purchaseLimit?.let { "${it}万" } ?: "不限")
                    }
                    
                    fund.updateTime.let { time ->
                        Text(
                            text = "更新时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(time))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumCard(premiumRate: Double?) {
    val isPositive = (premiumRate ?: 0.0) >= 0
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                premiumRate == null -> TextSecondary
                isPositive -> Positive
                else -> Negative
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = premiumRate?.let { "%+.2f%%".format(it) } ?: "-%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = CardBackground
            )
            Text(
                text = "溢价率",
                style = MaterialTheme.typography.bodyMedium,
                color = CardBackground.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary)
        Text(text = value, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}
