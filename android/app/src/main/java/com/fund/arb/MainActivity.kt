package com.fund.arb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fund.arb.ui.detail.DetailScreen
import com.fund.arb.ui.home.HomeScreen
import com.fund.arb.ui.lof.LOFScreen
import com.fund.arb.ui.qdii.QDIIScreen
import com.fund.arb.ui.theme.FundArbTheme
import com.fund.arb.ui.theme.Primary
import com.fund.arb.ui.watchlist.WatchlistScreen
import com.fund.arb.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "综合", Icons.Default.List)
    object QDII : Screen("qdii", "QDII", Icons.Default.Public)
    object LOF : Screen("lof", "LOF", Icons.Default.ShowChart)
    object Watchlist : Screen("watchlist", "自选", Icons.Default.Star)
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FundArbTheme {
                FundArbNavigation()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FundArbNavigation() {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = hiltViewModel()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "套利监控",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    IconButton(onClick = { homeViewModel.refreshAll() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = androidx.compose.ui.graphics.Color.White,
                    actionIconContentColor = androidx.compose.ui.graphics.Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                val screens = listOf(Screen.Home, Screen.QDII, Screen.LOF, Screen.Watchlist)
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onFundClick = { code -> navController.navigate("detail/$code") },
                    viewModel = homeViewModel
                )
            }
            composable(Screen.QDII.route) {
                QDIIScreen(onFundClick = { code -> navController.navigate("detail/$code") })
            }
            composable(Screen.LOF.route) {
                LOFScreen(onFundClick = { code -> navController.navigate("detail/$code") })
            }
            composable(Screen.Watchlist.route) {
                WatchlistScreen(onFundClick = { code -> navController.navigate("detail/$code") })
            }
            composable("detail/{code}") { backStackEntry ->
                val code = backStackEntry.arguments?.getString("code") ?: ""
                DetailScreen(
                    code = code,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
