package com.budgettracker.features.financialgoals.presentation

import com.budgettracker.features.financialgoals.presentation.dialogs.AddETFPortfolioDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.budgettracker.core.domain.model.ETFPortfolio
import com.budgettracker.core.domain.model.ETFHolding
import com.budgettracker.core.domain.model.PortfolioPerformance
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ETFPortfolioScreen(
    viewModel: ETFPortfolioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        floatingActionButton = {
            if (uiState.selectedPortfolio != null) {
                FloatingActionButton(
                    onClick = { viewModel.toggleAddHoldingDialog() },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, "Add Holding")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.selectedPortfolio == null) {
                EmptyPortfolioState(onAddClick = { viewModel.toggleAddPortfolioDialog() })
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        PortfolioSummaryCard(
                            portfolio = uiState.selectedPortfolio!!,
                            performance = uiState.performance,
                            holdings = uiState.holdings
                        )
                    }
                    
                    if (uiState.performance != null) {
                        item {
                            PerformanceMetricsCard(performance = uiState.performance!!)
                        }
                    }
                    
                    if (uiState.holdings.isNotEmpty()) {
                        item {
                            Text(
                                "Holdings (${uiState.holdings.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        items(uiState.holdings) { holding ->
                            HoldingCard(
                                holding = holding,
                                onClick = { viewModel.selectHolding(holding) }
                            )
                        }
                    } else {
                        item {
                            EmptyHoldingsMessage(onAddClick = { viewModel.toggleAddHoldingDialog() })
                        }
                    }
                }
            }
        }
        
        // Add Portfolio Dialog
        if (uiState.showAddPortfolioDialog) {
            AddETFPortfolioDialog(
                onDismiss = { viewModel.toggleAddPortfolioDialog() },
                onConfirm = { portfolio ->
                    viewModel.addPortfolio(portfolio)
                }
            )
        }
    }
}

@Composable
fun PortfolioSummaryCard(
    portfolio: ETFPortfolio,
    performance: PortfolioPerformance?,
    holdings: List<ETFHolding>
) {
    val totalValue = performance?.totalValue ?: 0.0
    val gainLoss = performance?.totalGainLoss ?: 0.0
    val gainLossPercentage = performance?.gainLossPercentage ?: 0f
    val isPositive = gainLoss >= 0
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                portfolio.brokerageName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                portfolio.accountType,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Total Value",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        NumberFormat.getCurrencyInstance().format(totalValue),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Gain/Loss",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        "${if (isPositive) "+" else ""}${NumberFormat.getCurrencyInstance().format(gainLoss)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isPositive) Color(0xFF28a745) else Color(0xFFdc3545)
                    )
                    Text(
                        "${if (isPositive) "+" else ""}${String.format("%.2f", gainLossPercentage)}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isPositive) Color(0xFF28a745) else Color(0xFFdc3545),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickStat("Holdings", holdings.size.toString())
                QuickStat("Cost Basis", NumberFormat.getCurrencyInstance().format(performance?.totalCostBasis ?: 0.0))
                QuickStat("Diversification", "${performance?.diversificationScore?.toInt() ?: 0}%")
            }
        }
    }
}

@Composable
fun QuickStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PerformanceMetricsCard(performance: PortfolioPerformance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Performance Metrics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem(
                    icon = Icons.Default.TrendingUp,
                    label = "Annual Dividends",
                    value = NumberFormat.getCurrencyInstance().format(performance.annualDividendIncome),
                    color = Color(0xFF28a745)
                )
                
                MetricItem(
                    icon = Icons.Default.Receipt,
                    label = "Annual Fees",
                    value = NumberFormat.getCurrencyInstance().format(performance.annualExpenseRatio),
                    color = Color(0xFFdc3545)
                )
            }
        }
    }
}

@Composable
fun MetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = color
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun HoldingCard(
    holding: ETFHolding,
    onClick: () -> Unit
) {
    val currentValue = holding.getCurrentValue()
    val gainLoss = holding.getGainLoss()
    val gainLossPercentage = holding.getGainLossPercentage()
    val isPositive = gainLoss >= 0
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    holding.ticker,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    holding.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        "${String.format("%.4f", holding.sharesOwned)} shares",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "@ ${NumberFormat.getCurrencyInstance().format(holding.currentPrice)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    NumberFormat.getCurrencyInstance().format(currentValue),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isPositive) Color(0xFF28a745) else Color(0xFFdc3545)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${if (isPositive) "+" else ""}${String.format("%.2f", gainLossPercentage)}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isPositive) Color(0xFF28a745) else Color(0xFFdc3545),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyHoldingsMessage(onAddClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.ShowChart,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No Holdings Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Start building your investment portfolio",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAddClick) {
                Icon(Icons.Default.Add, "Add")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add First Holding")
            }
        }
    }
}

@Composable
fun EmptyPortfolioState(onAddClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.AccountBalance,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No Portfolio Yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Create your investment portfolio",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onAddClick) {
                Icon(Icons.Default.Add, "Add")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Portfolio")
            }
        }
    }
}

