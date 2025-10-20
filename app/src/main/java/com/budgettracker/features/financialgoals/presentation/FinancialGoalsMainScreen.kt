package com.budgettracker.features.financialgoals.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Main Financial Goals screen with 4 tabs:
 * 1. Freedom Debt Journey
 * 2. Roth IRA
 * 3. Emergency Fund
 * 4. ETF Portfolio
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialGoalsMainScreen() {
    var selectedTabIndex by remember { mutableStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎯 Financial Goals") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                FinancialGoalTab.values().forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(tab.title) },
                        icon = {
                            Icon(
                                tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                }
            }
            
            // Tab Content
            when (selectedTabIndex) {
                0 -> DebtJourneyScreen()
                1 -> RothIRAScreen()
                2 -> EmergencyFundScreen()
                3 -> ETFPortfolioScreen()
            }
        }
    }
}

/**
 * Enum for Financial Goal tabs
 */
enum class FinancialGoalTab(
    val title: String,
    val icon: ImageVector
) {
    DEBT_JOURNEY("Debt Journey", Icons.Default.CreditCard),
    ROTH_IRA("Roth IRA", Icons.Default.Savings),
    EMERGENCY_FUND("Emergency Fund", Icons.Default.Shield),
    ETF_PORTFOLIO("ETF Portfolio", Icons.Default.TrendingUp)
}

