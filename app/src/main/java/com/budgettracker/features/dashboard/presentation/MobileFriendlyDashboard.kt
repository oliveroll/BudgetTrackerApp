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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgettracker.core.data.repository.FirebaseRepository
import com.budgettracker.ui.theme.Primary40
import com.budgettracker.ui.theme.Secondary40

/**
 * Mobile-friendly modern dashboard with beautiful design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileFriendlyDashboard(
    onNavigateToAddTransaction: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToBudget: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { FirebaseRepository(context) }
    
    // Real-time data
    val transactions by repository.getTransactionsFlow().collectAsState(initial = emptyList())
    val savingsGoals by repository.getSavingsGoalsFlow().collectAsState(initial = emptyList())
    val fixedExpenses by repository.getFixedExpensesFlow().collectAsState(initial = emptyList())
    
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
                ModernHeaderCard()
            }
            
            // Financial Overview Cards
            item {
                FinancialOverviewSection(
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
private fun ModernHeaderCard() {
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
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Welcome back! 👋",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        
                        Text(
                            text = "Oliver Ryan Ollesch",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.2f)
                            )
                        ) {
                            Text(
                                text = "Ixana Quasistatics • OPT Status",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                modifier = Modifier.padding(8.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // Profile Avatar
                    Card(
                        modifier = Modifier.size(60.dp),
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
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
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
                        fontSize = 18.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = transaction.category.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Text(
            text = if (transaction.type == com.budgettracker.core.domain.model.TransactionType.INCOME)
                "+$${String.format("%.2f", transaction.amount)}"
            else
                "-$${String.format("%.2f", transaction.amount)}",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (transaction.type == com.budgettracker.core.domain.model.TransactionType.INCOME)
                    Color(0xFF28a745)
                else
                    Color(0xFFdc3545)
            )
        )
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

// Data classes
private data class OverviewCardData(
    val title: String,
    val value: String,
    val color: Color,
    val icon: ImageVector
)

