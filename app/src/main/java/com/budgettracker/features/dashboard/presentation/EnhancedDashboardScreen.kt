package com.budgettracker.features.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgettracker.ui.theme.Primary40
import com.budgettracker.ui.theme.Secondary40

/**
 * Enhanced Dashboard screen matching the HTML design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedDashboardScreen(
    onNavigateToAddTransaction: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToBudget: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {}
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTransaction,
                containerColor = Primary40
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Transaction",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Header Section
            item {
                HeaderSection()
            }
            
            // Summary Cards
            item {
                SummaryCardsSection()
            }
            
            // Month Tabs
            item {
                MonthTabsSection()
            }
            
            // Quick Actions
            item {
                QuickActionsSection(
                    onNavigateToTransactions = onNavigateToTransactions,
                    onNavigateToBudget = onNavigateToBudget,
                    onNavigateToGoals = onNavigateToGoals
                )
            }
            
            // Recent Activity
            item {
                RecentActivitySection()
            }
            
            // Financial Goals Progress
            item {
                FinancialGoalsSection()
            }
        }
    }
}

@Composable
private fun HeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Primary40, Secondary40)
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Personal Budget Tracker 2025",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Ixana Quasistatics - $80,000 Base Salary - Indiana",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.9f)
                )
            )
        }
    }
}

@Composable
private fun SummaryCardsSection() {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F9FA))
            .padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(summaryCardData) { cardData ->
            SummaryCard(
                label = cardData.label,
                value = cardData.value,
                color = cardData.color,
                icon = cardData.icon
            )
        }
    }
}

@Composable
private fun SummaryCard(
    label: String,
    value: String,
    color: Color,
    icon: ImageVector
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .height(120.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthTabsSection() {
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    var selectedMonth by remember { mutableStateOf("September") }
    
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F9FA))
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(months) { month ->
            FilterChip(
                onClick = { selectedMonth = month },
                label = { Text(month) },
                selected = selectedMonth == month,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Primary40,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun QuickActionsSection(
    onNavigateToTransactions: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToGoals: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionButton(
                text = "Transactions",
                icon = Icons.Default.TrendingUp,
                onClick = onNavigateToTransactions,
                modifier = Modifier.weight(1f)
            )
            
            ActionButton(
                text = "Budget",
                icon = Icons.Default.AccountBalance,
                onClick = onNavigateToBudget,
                modifier = Modifier.weight(1f)
            )
            
            ActionButton(
                text = "Goals",
                icon = Icons.Default.Savings,
                onClick = onNavigateToGoals,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = Primary40.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Primary40,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = Primary40
                )
            )
        }
    }
}

@Composable
private fun RecentActivitySection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Sample recent transactions
            RecentTransactionItem(
                description = "Grocery Shopping",
                amount = "-$75.50",
                category = "Groceries",
                date = "Today"
            )
            
            RecentTransactionItem(
                description = "Salary Deposit",
                amount = "+$2,523.88",
                category = "Salary",
                date = "Sep 15"
            )
            
            RecentTransactionItem(
                description = "German Loan Payment",
                amount = "-$475.00",
                category = "Loan Payment",
                date = "Sep 20"
            )
        }
    }
}

@Composable
private fun RecentTransactionItem(
    description: String,
    amount: String,
    category: String,
    date: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            
            Text(
                text = "$category • $date",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        
        Text(
            text = amount,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (amount.startsWith("+")) Color(0xFF28a745) else Color(0xFFdc3545)
            )
        )
    }
}

@Composable
private fun FinancialGoalsSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📈 Financial Goals & Milestones",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            GoalProgressItem(
                title = "Emergency Fund Progress",
                current = "$0",
                target = "$16,000",
                progress = 0f
            )
            
            GoalProgressItem(
                title = "German Loan Remaining",
                current = "€11,000",
                target = "€0",
                progress = 0f
            )
            
            GoalProgressItem(
                title = "Roth IRA YTD",
                current = "$0",
                target = "$7,000",
                progress = 0f
            )
            
            GoalProgressItem(
                title = "Investment Portfolio",
                current = "$0",
                target = "Growing",
                progress = 0f
            )
        }
    }
}

@Composable
private fun GoalProgressItem(
    title: String,
    current: String,
    target: String,
    progress: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "$current / $target",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Primary40,
            trackColor = Color.LightGray.copy(alpha = 0.3f)
        )
    }
}

// Sample data
private data class SummaryCardData(
    val label: String,
    val value: String,
    val color: Color,
    val icon: ImageVector
)

private val summaryCardData = listOf(
    SummaryCardData(
        label = "Monthly Net Income",
        value = "$5,470",
        color = Color(0xFF28a745),
        icon = Icons.Default.TrendingUp
    ),
    SummaryCardData(
        label = "Fixed Expenses",
        value = "$1,706",
        color = Color(0xFFdc3545),
        icon = Icons.Default.AccountBalance
    ),
    SummaryCardData(
        label = "Savings & Investments",
        value = "$2,000",
        color = Color(0xFF007bff),
        icon = Icons.Default.Savings
    ),
    SummaryCardData(
        label = "Remaining Budget",
        value = "$1,764",
        color = Color(0xFF28a745),
        icon = Icons.Default.Add
    )
)
