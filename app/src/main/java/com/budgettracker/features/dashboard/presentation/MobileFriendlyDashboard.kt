package com.budgettracker.features.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgettracker.ui.theme.Primary40
import com.budgettracker.ui.theme.Secondary40
import com.budgettracker.core.data.local.TransactionDataStore
import com.budgettracker.core.utils.AnalyticsTracker

/**
 * Mobile-friendly modern dashboard with beautiful design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileFriendlyDashboard(
    onNavigateToAddTransaction: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToBudget: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    // Track screen view
    LaunchedEffect(Unit) {
        AnalyticsTracker.trackScreenViewed("Dashboard")
        AnalyticsTracker.trackDashboardViewed()
    }
    
    // Use persistent data that matches transactions tab
    var transactions by remember { mutableStateOf(TransactionDataStore.getTransactions()) }
    val savingsGoals = remember { getSampleSavingsGoals() }
    val fixedExpenses = remember { getSampleFixedExpenses() }
    
    // Load data from Firebase on dashboard load
    LaunchedEffect("data_load") {
        TransactionDataStore.initializeFromFirebase()
        transactions = TransactionDataStore.getTransactions()
    }
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTransaction,
                containerColor = Primary40,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Transaction",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Beautiful Header with Gradient
            item {
                ModernHeaderCard(onNavigateToSettings = onNavigateToSettings)
            }
            
            // Financial Overview Cards
            item {
                FinancialOverviewSection(
                    transactions = transactions,
                    fixedExpenses = fixedExpenses
                )
            }
            
            // Visual Charts Section
            item {
                VisualChartsSection(
                    transactions = transactions,
                    fixedExpenses = fixedExpenses
                )
            }
            
            // Quick Actions Grid
            item {
                QuickActionsGrid(
                    onNavigateToTransactions = onNavigateToTransactions,
                    onNavigateToBudget = onNavigateToBudget,
                    onNavigateToGoals = onNavigateToGoals,
                    onNavigateToAddTransaction = onNavigateToAddTransaction
                )
            }
            
            // Recent Activity
            item {
                RecentActivityCard(transactions = transactions.take(3))
            }
            
            // Goals Progress
            item {
                GoalsProgressCard(goals = savingsGoals.take(2))
            }
            
            // Monthly Insights
            item {
                MonthlyInsightsCard(
                    transactions = transactions,
                    fixedExpenses = fixedExpenses
                )
            }
        }
    }
}

@Composable
private fun ModernHeaderCard(
    onNavigateToSettings: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Primary40, Secondary40)
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Welcome back! 👋",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    
                    Text(
                        text = "Oliver Ryan",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 1
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Ixana • OPT Status",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Settings Icon
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Profile Avatar
                Card(
                    modifier = Modifier.size(50.dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "O",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FinancialOverviewSection(
    transactions: List<com.budgettracker.core.domain.model.Transaction>,
    fixedExpenses: List<com.budgettracker.core.domain.model.FixedExpense>
) {
    val monthlyIncome = transactions.filter { 
        it.type == com.budgettracker.core.domain.model.TransactionType.INCOME 
    }.sumOf { it.amount }
    
    val monthlyExpenses = fixedExpenses.sumOf { it.amount }
    val remaining = 5470.0 - monthlyExpenses // Based on your income
    
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(
            listOf(
                OverviewCardData("Monthly Income", "$5,470", Color(0xFF28a745), Icons.Default.TrendingUp),
                OverviewCardData("Fixed Expenses", "$${String.format("%.0f", monthlyExpenses)}", Color(0xFFdc3545), Icons.Default.Receipt),
                OverviewCardData("Remaining", "$${String.format("%.0f", remaining)}", Color(0xFF007bff), Icons.Default.Savings),
                OverviewCardData("Savings Rate", "${String.format("%.0f", (2000.0/5470.0)*100)}%", Color(0xFF6f42c1), Icons.Default.TrendingUp)
            )
        ) { cardData ->
            ModernOverviewCard(cardData)
        }
    }
}

@Composable
private fun ModernOverviewCard(data: OverviewCardData) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = data.color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = data.icon,
                    contentDescription = data.title,
                    tint = data.color,
                    modifier = Modifier.size(24.dp)
                )
                
                Card(
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = data.color.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.size(8.dp)
                ) {}
            }
            
            Column {
                Text(
                    text = data.value,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = data.color
                    )
                )
                
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun QuickActionsGrid(
    onNavigateToTransactions: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToAddTransaction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModernActionCard(
                title = "Add Transaction",
                subtitle = "Record income/expense",
                icon = Icons.Default.Add,
                color = Primary40,
                onClick = onNavigateToAddTransaction,
                modifier = Modifier.weight(1f)
            )
            
            ModernActionCard(
                title = "View Budget",
                subtitle = "Track spending",
                icon = Icons.Default.PieChart,
                color = Color(0xFF17a2b8),
                onClick = onNavigateToBudget,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModernActionCard(
                title = "Savings Goals",
                subtitle = "Track progress",
                icon = Icons.Default.Savings,
                color = Color(0xFF28a745),
                onClick = onNavigateToGoals,
                modifier = Modifier.weight(1f)
            )
            
            ModernActionCard(
                title = "Transactions",
                subtitle = "View history",
                icon = Icons.Default.Receipt,
                color = Color(0xFFfd7e14),
                onClick = onNavigateToTransactions,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RecentActivityCard(
    transactions: List<com.budgettracker.core.domain.model.Transaction>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "History",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (transactions.isEmpty()) {
                Text(
                    text = "No recent transactions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                transactions.forEach { transaction ->
                    ModernTransactionItem(transaction = transaction)
                    if (transaction != transactions.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernTransactionItem(
    transaction: com.budgettracker.core.domain.model.Transaction
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon and details section
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = if (transaction.type == com.budgettracker.core.domain.model.TransactionType.INCOME)
                        Color(0xFF28a745).copy(alpha = 0.1f)
                    else
                        Color(0xFFdc3545).copy(alpha = 0.1f)
                ),
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = transaction.category.icon,
                        fontSize = 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                    Text(
                        // FIXED: Use DateTimeFormatter for LocalDate
                        text = "${transaction.category.displayName} • ${transaction.date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd", java.util.Locale.getDefault()))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Amount section - fixed width to prevent squeezing
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.widthIn(min = 80.dp)
        ) {
            Text(
                text = if (transaction.type == com.budgettracker.core.domain.model.TransactionType.INCOME)
                    "+$${String.format("%.0f", transaction.amount)}"
                else
                    "-$${String.format("%.0f", transaction.amount)}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.type == com.budgettracker.core.domain.model.TransactionType.INCOME)
                        Color(0xFF28a745)
                    else
                        Color(0xFFdc3545)
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun GoalsProgressCard(
    goals: List<com.budgettracker.core.domain.model.SavingsGoal>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Savings Goals",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = "Goals",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (goals.isEmpty()) {
                Text(
                    text = "No savings goals yet. Create your first goal!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                goals.forEach { goal ->
                    ModernGoalProgressItem(goal = goal)
                    if (goal != goals.last()) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernGoalProgressItem(
    goal: com.budgettracker.core.domain.model.SavingsGoal
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${goal.icon} ${goal.name}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "$${String.format("%.0f", goal.currentAmount)} / $${String.format("%.0f", goal.targetAmount)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color(android.graphics.Color.parseColor(goal.color))
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = goal.getProgressPercentage() / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Color(android.graphics.Color.parseColor(goal.color)),
            trackColor = Color.LightGray.copy(alpha = 0.3f)
        )
    }
}

@Composable
private fun MonthlyInsightsCard(
    transactions: List<com.budgettracker.core.domain.model.Transaction>,
    fixedExpenses: List<com.budgettracker.core.domain.model.FixedExpense>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "💡 Monthly Insights",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val insights = listOf(
                "Emergency Fund Target: $32,820 (6 months for OPT)",
                "German Loan: €11,000 remaining @ €450/month",
                "Savings Rate: 36.6% (Above recommended 20%)",
                "Next Major Expense: Phone renewal in February"
            )
            
            insights.forEach { insight ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "• ",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = insight,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun VisualChartsSection(
    transactions: List<com.budgettracker.core.domain.model.Transaction>,
    fixedExpenses: List<com.budgettracker.core.domain.model.FixedExpense>
) {
    val monthlyExpenses = remember(fixedExpenses) { 
        fixedExpenses.sumOf { it.amount } 
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Financial Overview",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Income vs Expenses Chart
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Income vs Expenses",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                IncomeExpenseChart(
                    monthlyIncome = 5470.0,
                    monthlyExpenses = monthlyExpenses
                )
            }
        }
        
        // Spending by Category Chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Spending by Category",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                SpendingCategoryChart(transactions = transactions)
            }
        }
    }
}

@Composable
private fun IncomeExpenseChart(
    monthlyIncome: Double,
    monthlyExpenses: Double
) {
    val remaining = monthlyIncome - monthlyExpenses
    
    // Prevent division by zero and infinite recomposition
    val safeIncome = if (monthlyIncome <= 0) 1.0 else monthlyIncome
    val safeExpenses = if (monthlyExpenses < 0) 0.0 else monthlyExpenses.coerceAtMost(safeIncome)
    val safeRemaining = safeIncome - safeExpenses
    
    val incomePercentage = 1f
    val expensePercentage = (safeExpenses / safeIncome).toFloat().coerceIn(0f, 1f)
    val remainingPercentage = (safeRemaining / safeIncome).toFloat().coerceIn(0f, 1f)
    
    Column {
        // Visual Bar Chart
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Income Bar
            Card(
                modifier = Modifier
                    .weight(incomePercentage)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF28a745)
                ),
                shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Income",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Expenses Bar
            Card(
                modifier = Modifier
                    .weight(expensePercentage)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFdc3545)
                ),
                shape = RoundedCornerShape(0.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Expenses",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Remaining Bar
            Card(
                modifier = Modifier
                    .weight(remainingPercentage)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF007bff)
                ),
                shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Saved",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Legend with values
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ChartLegendItem(
                color = Color(0xFF28a745),
                label = "Income",
                value = "$${String.format("%.0f", safeIncome)}"
            )
            ChartLegendItem(
                color = Color(0xFFdc3545),
                label = "Expenses",
                value = "$${String.format("%.0f", safeExpenses)}"
            )
            ChartLegendItem(
                color = Color(0xFF007bff),
                label = "Remaining",
                value = "$${String.format("%.0f", safeRemaining)}"
            )
        }
    }
}

@Composable
private fun SpendingCategoryChart(
    transactions: List<com.budgettracker.core.domain.model.Transaction>
) {
    val expenseTransactions = transactions.filter { 
        it.type == com.budgettracker.core.domain.model.TransactionType.EXPENSE 
    }
    
    if (expenseTransactions.isEmpty()) {
        Text(
            text = "No expense data available",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
        )
        return
    }
    
    val categoryTotals = expenseTransactions
        .groupBy { it.category.displayName }
        .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }
        .take(5)
    
    val total = categoryTotals.sumOf { it.second }
    val colors = listOf(
        Color(0xFF007bff),
        Color(0xFF28a745),
        Color(0xFFffc107),
        Color(0xFFdc3545),
        Color(0xFF6f42c1)
    )
    
    Column {
        // Simple Pie Chart representation using bars
        categoryTotals.forEachIndexed { index, (category, amount) ->
            val percentage = (amount / total * 100).toFloat()
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color indicator
                Card(
                    modifier = Modifier.size(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colors[index % colors.size]
                    ),
                    shape = CircleShape
                ) {}
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Category name
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Medium
                )
                
                // Percentage and amount
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${String.format("%.1f", percentage)}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = colors[index % colors.size]
                    )
                    Text(
                        text = "$${String.format("%.0f", amount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Progress bar
            LinearProgressIndicator(
                progress = percentage / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = colors[index % colors.size],
                trackColor = Color.LightGray.copy(alpha = 0.3f)
            )
            
            if (index < categoryTotals.size - 1) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ChartLegendItem(
    color: Color,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.size(12.dp),
                colors = CardDefaults.cardColors(containerColor = color),
                shape = CircleShape
            ) {}
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

// Sample data functions to prevent ANR
private fun getSampleTransactions(): List<com.budgettracker.core.domain.model.Transaction> {
    return listOf(
        com.budgettracker.core.domain.model.Transaction(
            id = "1",
            userId = "demo_user",
            amount = 2523.88,
            category = com.budgettracker.core.domain.model.TransactionCategory.SALARY,
            type = com.budgettracker.core.domain.model.TransactionType.INCOME,
            description = "Salary Deposit - Ixana Quasistatics",
            date = java.time.LocalDate.now(),
            notes = "Bi-weekly salary"
        ),
        com.budgettracker.core.domain.model.Transaction(
            id = "2",
            userId = "demo_user",
            amount = 475.0,
            category = com.budgettracker.core.domain.model.TransactionCategory.LOAN_PAYMENT,
            type = com.budgettracker.core.domain.model.TransactionType.EXPENSE,
            description = "German Student Loan Payment",
            date = java.time.LocalDate.now().minusDays(1), // Yesterday
            notes = "€450 monthly payment"
        ),
        com.budgettracker.core.domain.model.Transaction(
            id = "3",
            userId = "demo_user",
            amount = 75.50,
            category = com.budgettracker.core.domain.model.TransactionCategory.GROCERIES,
            type = com.budgettracker.core.domain.model.TransactionType.EXPENSE,
            description = "Grocery Shopping - Walmart",
            date = java.time.LocalDate.now(), // Today
            notes = "Weekly groceries"
        )
    )
}

private fun getSampleSavingsGoals(): List<com.budgettracker.core.domain.model.SavingsGoal> {
    return listOf(
        com.budgettracker.core.domain.model.SavingsGoal(
            id = "goal_1",
            userId = "demo_user",
            name = "Emergency Fund",
            description = "6 months of expenses for OPT visa security",
            targetAmount = 16000.0,
            currentAmount = 5200.0,
            deadline = java.util.Date(System.currentTimeMillis() + 15552000000L), // 6 months
            priority = com.budgettracker.core.domain.model.Priority.CRITICAL,
            monthlyContribution = 800.0,
            category = com.budgettracker.core.domain.model.GoalCategory.EMERGENCY_FUND
        ),
        com.budgettracker.core.domain.model.SavingsGoal(
            id = "goal_2",
            userId = "demo_user",
            name = "Roth IRA 2025",
            description = "Tax-free retirement savings",
            targetAmount = 7000.0,
            currentAmount = 1400.0,
            deadline = java.util.Date(System.currentTimeMillis() + 31104000000L), // 12 months
            priority = com.budgettracker.core.domain.model.Priority.HIGH,
            monthlyContribution = 583.0,
            category = com.budgettracker.core.domain.model.GoalCategory.RETIREMENT
        )
    )
}

private fun getSampleFixedExpenses(): List<com.budgettracker.core.domain.model.FixedExpense> {
    return listOf(
        com.budgettracker.core.domain.model.FixedExpense(
            id = "exp_1",
            userId = "demo_user",
            category = "Housing",
            description = "Rent",
            amount = 1200.0,
            dueDate = "1st"
        ),
        com.budgettracker.core.domain.model.FixedExpense(
            id = "exp_2",
            userId = "demo_user",
            category = "Debt",
            description = "German Student Loan",
            amount = 475.0,
            dueDate = "20th"
        ),
        com.budgettracker.core.domain.model.FixedExpense(
            id = "exp_3",
            userId = "demo_user",
            category = "Insurance",
            description = "Car Insurance",
            amount = 150.0,
            dueDate = "15th"
        ),
        com.budgettracker.core.domain.model.FixedExpense(
            id = "exp_4",
            userId = "demo_user",
            category = "Utilities",
            description = "Phone Plan",
            amount = 45.0,
            dueDate = "10th"
        )
    )
}

// Data classes
private data class OverviewCardData(
    val title: String,
    val value: String,
    val color: Color,
    val icon: ImageVector
)

