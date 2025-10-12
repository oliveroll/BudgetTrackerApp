package com.budgettracker.features.budget.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.budgettracker.core.data.local.dao.DashboardSummary
import com.budgettracker.core.data.local.dao.TimelineItem
import com.budgettracker.core.data.local.entities.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Enhanced Budget Overview Screen with comprehensive financial tracking
 * Follows Material 3 design with mobile-first responsive layout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedBudgetOverviewScreen(
    onNavigateToSubscriptions: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {},
    onNavigateToDebtTracker: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: BudgetOverviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showEditBalanceDialog by viewModel.showEditBalanceDialog.collectAsState()
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf<EssentialExpenseEntity?>(null) }
    val context = LocalContext.current
    
    // Animation states
    val isDataLoaded by remember { mutableStateOf(true) }
    
    // Handle success/error messages with animations
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            // Show toast or snackbar with animation
            viewModel.clearSuccessMessage()
        }
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            // Show error toast or snackbar with animation
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "💰 Budget Overview",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                        Text(
                            text = "Last updated: ${viewModel.getFormattedLastUpdated()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = if (uiState.isLoading) MaterialTheme.colorScheme.primary 
                                  else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddExpenseDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Essential Expense"
                )
            }
        }
    ) { paddingValues ->
        
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }
                
                // Financial Snapshot Card with Animation
                item {
                    AnimatedVisibility(
                        visible = isDataLoaded,
                        enter = slideInVertically(
                            initialOffsetY = { -40 },
                            animationSpec = tween(600, delayMillis = 100)
                        ) + fadeIn(animationSpec = tween(600, delayMillis = 100))
                    ) {
                        FinancialSnapshotCard(
                            currentBalance = uiState.currentBalance,
                            monthlyIncome = uiState.monthlyIncome,
                            upcomingPaychecks = uiState.upcomingPaychecks.take(2),
                            onEditBalance = { viewModel.showEditBalanceDialog() },
                            onMarkPaycheckDeposited = { viewModel.markPaycheckDeposited(it) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
                
                // Health Score Card with Animation
                item {
                    AnimatedVisibility(
                        visible = isDataLoaded,
                        enter = slideInVertically(
                            initialOffsetY = { -40 },
                            animationSpec = tween(600, delayMillis = 200)
                        ) + fadeIn(animationSpec = tween(600, delayMillis = 200))
                    ) {
                        FinancialHealthCard(
                            healthScore = viewModel.getHealthScore(),
                            netRemaining = viewModel.getNetRemaining(),
                            totalSubscriptions = viewModel.getTotalMonthlySubscriptions(),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
                
                // Essential Expenses Section with Animation
                item {
                    AnimatedVisibility(
                        visible = isDataLoaded,
                        enter = slideInVertically(
                            initialOffsetY = { -40 },
                            animationSpec = tween(600, delayMillis = 300)
                        ) + fadeIn(animationSpec = tween(600, delayMillis = 300))
                    ) {
                        EssentialExpensesSection(
                            expenses = uiState.essentialExpenses,
                            dashboardSummary = uiState.dashboardSummary,
                            onMarkPaid = { expenseId, amount -> 
                                viewModel.markEssentialPaid(expenseId, amount) 
                            },
                            onDeleteExpense = { expense ->
                                showDeleteConfirmDialog = expense
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
                
                // Active Subscriptions Section with Animation
                item {
                    AnimatedVisibility(
                        visible = isDataLoaded,
                        enter = slideInVertically(
                            initialOffsetY = { -40 },
                            animationSpec = tween(600, delayMillis = 400)
                        ) + fadeIn(animationSpec = tween(600, delayMillis = 400))
                    ) {
                        ActiveSubscriptionsSection(
                            subscriptions = uiState.subscriptions,
                            onNavigateToManage = onNavigateToSubscriptions,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
                
                // Upcoming Timeline Section with Animation
                item {
                    AnimatedVisibility(
                        visible = isDataLoaded,
                        enter = slideInVertically(
                            initialOffsetY = { -40 },
                            animationSpec = tween(600, delayMillis = 500)
                        ) + fadeIn(animationSpec = tween(600, delayMillis = 500))
                    ) {
                        UpcomingTimelineSection(
                            timeline = uiState.upcomingTimeline.take(10),
                            onViewAll = onNavigateToReminders,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
                
                // Quick Actions with Animation
                item {
                    AnimatedVisibility(
                        visible = isDataLoaded,
                        enter = slideInVertically(
                            initialOffsetY = { -40 },
                            animationSpec = tween(600, delayMillis = 600)
                        ) + fadeIn(animationSpec = tween(600, delayMillis = 600))
                    ) {
                        QuickActionsSection(
                            onNavigateToSubscriptions = onNavigateToSubscriptions,
                            onNavigateToReminders = onNavigateToReminders,
                            onNavigateToDebtTracker = onNavigateToDebtTracker,
                            onSeedSampleData = { viewModel.seedSampleData() },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
    
    // Edit Balance Dialog
    if (showEditBalanceDialog) {
        EditBalanceDialog(
            currentBalance = uiState.currentBalance,
            onConfirm = { newBalance -> viewModel.updateBalance(newBalance) },
            onDismiss = { viewModel.hideEditBalanceDialog() }
        )
    }
    
    // Add Essential Expense Dialog
    if (showAddExpenseDialog) {
        AddEssentialExpenseDialog(
            onConfirm = { name, category, amount, dueDay ->
                viewModel.addEssentialExpense(name, category, amount, dueDay)
                showAddExpenseDialog = false
            },
            onDismiss = { showAddExpenseDialog = false }
        )
    }
    
    // Delete Confirmation Dialog
    showDeleteConfirmDialog?.let { expense ->
        DeleteExpenseConfirmDialog(
            expense = expense,
            onConfirm = {
                viewModel.deleteEssentialExpense(expense.id)
                showDeleteConfirmDialog = null
            },
            onDismiss = { showDeleteConfirmDialog = null }
        )
    }
}

@Composable
private fun FinancialSnapshotCard(
    currentBalance: Double,
    monthlyIncome: Double,
    upcomingPaychecks: List<PaycheckEntity>,
    onEditBalance: () -> Unit,
    onMarkPaycheckDeposited: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF667eea),
                            Color(0xFF764ba2)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header with icon and title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💰",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Current Balance",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    IconButton(
                        onClick = onEditBalance,
                        modifier = Modifier
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Balance",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Animated balance value
                val animatedBalance by animateFloatAsState(
                    targetValue = currentBalance.toFloat(),
                    animationSpec = tween(1200, easing = EaseOutCubic),
                    label = "balance_animation"
                )
                
                Text(
                    text = "$${String.format("%.2f", animatedBalance)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 32.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Monthly Income with health indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Monthly Net Income",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "$${String.format("%.2f", monthlyIncome)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    // Health indicator circle
                    val healthPercentage = (currentBalance / monthlyIncome).coerceIn(0.0, 1.0)
                    HealthIndicatorCircle(
                        percentage = healthPercentage.toFloat(),
                        modifier = Modifier.size(50.dp)
                    )
                }
                
                // Upcoming Paychecks
                if (upcomingPaychecks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Next Paychecks",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    upcomingPaychecks.forEach { paycheck ->
                        HomeStylePaycheckItem(
                            paycheck = paycheck,
                            onMarkDeposited = { onMarkPaycheckDeposited(paycheck.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthIndicatorCircle(
    percentage: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 1200, easing = EaseOutCubic),
        label = "health_indicator"
    )
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val strokeWidth = 4.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            
            // Background circle
            drawCircle(
                color = Color.White.copy(alpha = 0.3f),
                radius = radius,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            
            // Progress arc
            drawArc(
                color = Color.White,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        Text(
            text = "${(animatedProgress * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun AnimatedPaycheckItem(
    paycheck: PaycheckEntity,
    onMarkDeposited: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = dateFormat.format(Date(paycheck.date)),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                text = "$${String.format("%.2f", paycheck.netAmount)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
        
        if (!paycheck.deposited && paycheck.getDaysUntilPaycheck() <= 0) {
            TextButton(
                onClick = onMarkDeposited,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(8.dp)
                    )
            ) {
                Text("Mark Deposited")
            }
        }
    }
}

@Composable
private fun AnimatedSummaryItem(
    label: String, 
    value: Double,
    color: Color,
    isAmount: Boolean = true
) {
    val animatedValue by animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "summary_animation"
    )
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (isAmount) "$${String.format("%.2f", animatedValue)}" else "${animatedValue.toInt()}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color,
            fontSize = 20.sp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AnimatedEssentialExpenseItem(
    expense: EssentialExpenseEntity,
    onMarkPaid: (Double?) -> Unit,
    onDelete: () -> Unit
) {
    val isOverdue = expense.isOverdue()
    val daysUntilDue = expense.getDaysUntilDue()
    var showDeleteButton by remember { mutableStateOf(false) }
    
    // Animation for paid state
    val animatedAlpha by animateFloatAsState(
        targetValue = if (expense.paid) 0.6f else 1f,
        animationSpec = tween(300),
        label = "paid_alpha"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
            .alpha(animatedAlpha),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = expense.category.iconEmoji,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 24.sp
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = expense.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                
                Text(
                    text = when {
                        expense.paid -> "✓ Paid"
                        isOverdue -> "⚠️ Overdue"
                        daysUntilDue != null && daysUntilDue == 0 -> "Due Today"
                        daysUntilDue != null && daysUntilDue > 0 -> "Due in $daysUntilDue days"
                        else -> expense.category.displayName
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        expense.paid -> Color(0xFF4CAF50)
                        isOverdue -> Color(0xFFFF5722)
                        daysUntilDue != null && daysUntilDue <= 3 -> Color(0xFFFFEB3B)
                        else -> Color.White.copy(alpha = 0.8f)
                    }
                )
            }
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$${String.format("%.2f", expense.actualAmount ?: expense.plannedAmount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            if (!expense.paid) {
                // Checkbox with animation
                AnimatedVisibility(
                    visible = !showDeleteButton,
                    enter = scaleIn(animationSpec = tween(200)),
                    exit = scaleOut(animationSpec = tween(200))
                ) {
                    Checkbox(
                        checked = false,
                        onCheckedChange = { 
                            if (it) onMarkPaid(null)
                        },
                        colors = CheckboxDefaults.colors(
                            uncheckedColor = Color.White.copy(alpha = 0.7f),
                            checkedColor = Color(0xFF4CAF50)
                        )
                    )
                }
                
                // Long press to show delete
                IconButton(
                    onClick = { showDeleteButton = !showDeleteButton },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                // Delete button with animation
                AnimatedVisibility(
                    visible = showDeleteButton,
                    enter = slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(),
                    exit = slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeOut()
                ) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .background(
                                Color(0xFFFF5722).copy(alpha = 0.8f),
                                CircleShape
                            )
                            .size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FinancialHealthCard(
    healthScore: Int,
    netRemaining: Double,
    totalSubscriptions: Double,
    modifier: Modifier = Modifier
) {
    // Create overview cards similar to Home tab
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(
            listOf(
                OverviewCardData("Health Score", "$healthScore%", getHealthColor(healthScore), Icons.Default.TrendingUp),
                OverviewCardData("Net Remaining", "$${String.format("%.0f", netRemaining)}", if (netRemaining > 0) Color(0xFF28a745) else Color(0xFFdc3545), Icons.Default.AccountBalance),
                OverviewCardData("Subscriptions", "$${String.format("%.0f", totalSubscriptions)}", Color(0xFF6f42c1), Icons.Default.Subscriptions),
                OverviewCardData("Savings Rate", "${if (netRemaining > 0) String.format("%.0f", (netRemaining/5191.32)*100) else "0"}%", Color(0xFF007bff), Icons.Default.Savings)
            )
        ) { cardData ->
            HomeStyleOverviewCard(cardData)
        }
    }
}

@Composable
private fun HomeStyleOverviewCard(data: OverviewCardData) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = data.color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = data.icon,
                    contentDescription = null,
                    tint = data.color,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Column {
                Text(
                    text = data.value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = data.color
                )
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

private fun getHealthColor(healthScore: Int): Color {
    return when {
        healthScore >= 80 -> Color(0xFF28a745) // Green
        healthScore >= 60 -> Color(0xFFffc107) // Yellow
        else -> Color(0xFFdc3545) // Red
    }
}

data class OverviewCardData(
    val title: String,
    val value: String,
    val color: Color,
    val icon: ImageVector
)

@Composable
private fun HealthScoreCircle(score: Int) {
    val animatedProgress by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = tween(durationMillis = 1500, easing = EaseOutCubic),
        label = "health_score"
    )
    
    val color = when {
        score >= 80 -> Color(0xFF4CAF50) // Green
        score >= 60 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336) // Red
    }
    
    Box(contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier.size(80.dp)
        ) {
            val strokeWidth = 8.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            
            // Background circle
            drawCircle(
                color = color.copy(alpha = 0.2f),
                radius = radius,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            
            // Progress arc
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        Text(
            text = "${(animatedProgress * 100).toInt()}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    isPositive: Boolean
) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isPositive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun EssentialExpensesSection(
    expenses: List<EssentialExpenseEntity>,
    dashboardSummary: DashboardSummary?,
    onMarkPaid: (String, Double?) -> Unit,
    onDeleteExpense: (EssentialExpenseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMonth = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header with month
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏠",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Essential Expenses",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = currentMonth,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Home-style summary cards
            dashboardSummary?.let { summary ->
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(
                        listOf(
                            OverviewCardData("Total Planned", "$${String.format("%.0f", summary.totalPlanned ?: 0.0)}", Color(0xFF007bff), Icons.Default.Assignment),
                            OverviewCardData("Total Paid", "$${String.format("%.0f", summary.totalPaid ?: 0.0)}", Color(0xFF28a745), Icons.Default.CheckCircle),
                            OverviewCardData("Remaining", "$${String.format("%.0f", (summary.totalPlanned ?: 0.0) - (summary.totalPaid ?: 0.0))}", Color(0xFFffc107), Icons.Default.Schedule),
                            OverviewCardData("Items Left", "${summary.unpaidCount ?: 0}", Color(0xFFdc3545), Icons.Default.List)
                        )
                    ) { cardData ->
                        CompactOverviewCard(cardData)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Expenses List with Home-style cards
            if (expenses.isNotEmpty()) {
                expenses.forEach { expense ->
                    HomeStyleExpenseItem(
                        expense = expense,
                        onMarkPaid = { amount -> onMarkPaid(expense.id, amount) },
                        onDelete = { onDeleteExpense(expense) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No expenses for $currentMonth",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Tap + to add your first expense",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactOverviewCard(data: OverviewCardData) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(80.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = data.color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = data.icon,
                contentDescription = null,
                tint = data.color,
                modifier = Modifier.size(16.dp)
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = data.value,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = data.color,
                    maxLines = 1
                )
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun HomeStyleExpenseItem(
    expense: EssentialExpenseEntity,
    onMarkPaid: (Double?) -> Unit,
    onDelete: () -> Unit
) {
    val isOverdue = expense.isOverdue()
    val daysUntilDue = expense.getDaysUntilDue()
    var showDeleteButton by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                expense.paid -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                isOverdue -> Color(0xFFdc3545).copy(alpha = 0.1f)
                daysUntilDue != null && daysUntilDue <= 3 -> Color(0xFFffc107).copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category icon
                Card(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = expense.category.iconEmoji,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = expense.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (expense.paid) 
                            MaterialTheme.colorScheme.onSurfaceVariant 
                            else MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = when {
                            expense.paid -> "✓ Paid"
                            isOverdue -> "⚠️ Overdue"
                            daysUntilDue != null && daysUntilDue == 0 -> "Due Today"
                            daysUntilDue != null && daysUntilDue > 0 -> "Due in $daysUntilDue days"
                            else -> expense.category.displayName
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            expense.paid -> Color(0xFF28a745)
                            isOverdue -> Color(0xFFdc3545)
                            daysUntilDue != null && daysUntilDue <= 3 -> Color(0xFFffc107)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$${String.format("%.2f", expense.actualAmount ?: expense.plannedAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                if (!expense.paid) {
                    if (!showDeleteButton) {
                        Checkbox(
                            checked = false,
                            onCheckedChange = { 
                                if (it) onMarkPaid(null)
                            },
                            colors = CheckboxDefaults.colors(
                                uncheckedColor = MaterialTheme.colorScheme.outline,
                                checkedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    
                    IconButton(
                        onClick = { showDeleteButton = !showDeleteButton },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (showDeleteButton) Icons.Default.Delete else Icons.Default.MoreVert,
                            contentDescription = if (showDeleteButton) "Delete" else "Options",
                            tint = if (showDeleteButton) Color(0xFFdc3545) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    if (showDeleteButton) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Confirm Delete",
                                tint = Color(0xFFdc3545),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EssentialExpenseItem(
    expense: EssentialExpenseEntity,
    onMarkPaid: (Double?) -> Unit
) {
    val isOverdue = expense.isOverdue()
    val daysUntilDue = expense.getDaysUntilDue()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = when {
                    expense.paid -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    isOverdue -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    daysUntilDue != null && daysUntilDue <= 3 -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = expense.category.iconEmoji,
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = expense.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (expense.paid) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                        else MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = when {
                        expense.paid -> "✓ Paid"
                        isOverdue -> "⚠️ Overdue"
                        daysUntilDue != null && daysUntilDue == 0 -> "Due Today"
                        daysUntilDue != null && daysUntilDue > 0 -> "Due in $daysUntilDue days"
                        else -> expense.category.displayName
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        expense.paid -> Color(0xFF4CAF50)
                        isOverdue -> MaterialTheme.colorScheme.error
                        daysUntilDue != null && daysUntilDue <= 3 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$${String.format("%.2f", expense.actualAmount ?: expense.plannedAmount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            if (!expense.paid) {
                Checkbox(
                    checked = false,
                    onCheckedChange = { onMarkPaid(null) },
                    colors = CheckboxDefaults.colors(
                        uncheckedColor = MaterialTheme.colorScheme.outline,
                        checkedColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@Composable
private fun ActiveSubscriptionsSection(
    subscriptions: List<EnhancedSubscriptionEntity>,
    onNavigateToManage: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter only truly active subscriptions to fix counting issue
    val activeSubscriptions = subscriptions.filter { it.active }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔔",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Active Subscriptions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                TextButton(onClick = onNavigateToManage) {
                    Text("Manage", color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (activeSubscriptions.isNotEmpty()) {
                activeSubscriptions.take(3).forEach { subscription ->
                    HomeStyleSubscriptionItem(subscription = subscription)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                if (activeSubscriptions.size > 3) {
                    Text(
                        text = "... and ${activeSubscriptions.size - 3} more",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }
            } else {
                Text(
                    text = "No active subscriptions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun HomeStyleSubscriptionItem(
    subscription: EnhancedSubscriptionEntity
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val daysUntil = subscription.getDaysUntilBilling()
    val isUrgent = daysUntil <= 3 && daysUntil >= 0
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUrgent) 
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = subscription.iconEmoji ?: "💳",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = subscription.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = "Next: ${dateFormat.format(Date(subscription.nextBillingDate))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$${String.format("%.2f", subscription.amount)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isUrgent) MaterialTheme.colorScheme.error
                           else MaterialTheme.colorScheme.primary
                )
                
                if (daysUntil >= 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isUrgent) MaterialTheme.colorScheme.errorContainer
                               else MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = when (daysUntil) {
                                0 -> "Due today"
                                1 -> "Due tomorrow"
                                else -> "$daysUntil days"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isUrgent) MaterialTheme.colorScheme.onErrorContainer
                                   else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeStylePaycheckItem(
    paycheck: PaycheckEntity,
    onMarkDeposited: () -> Unit = {}
) {
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    val isDeposited = paycheck.deposited
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDeposited) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDeposited)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = if (isDeposited) "✅" else "💰",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = "Paycheck",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = dateFormat.format(Date(paycheck.date)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "+$${String.format("%.2f", paycheck.netAmount)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDeposited) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.secondary
                )
                
                if (!isDeposited) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable { onMarkDeposited() }
                    ) {
                        Text(
                            text = "Mark Deposited",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UpcomingTimelineSection(
    timeline: List<TimelineItem>,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
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
                    text = "📅 Next 30 Days",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                TextButton(onClick = onViewAll) {
                    Text("View All")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (timeline.isNotEmpty()) {
                timeline.forEach { item ->
                    TimelineItemRow(item = item)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                Text(
                    text = "No upcoming items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun TimelineItemRow(
    item: TimelineItem
) {
    val icon = when (item.type) {
        "ESSENTIAL" -> "🏠"
        "SUBSCRIPTION" -> "💳"
        "PAYCHECK" -> "💰"
        else -> "📅"
    }
    
    val color = when (item.type) {
        "PAYCHECK" -> Color(0xFF4CAF50) // Green for income
        else -> MaterialTheme.colorScheme.primary // Default for expenses
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = item.dateStr.take(10), // Show date part only
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Text(
            text = "${if (item.type == "PAYCHECK") "+" else "-"}$${String.format("%.2f", item.amount)}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun QuickActionsSection(
    onNavigateToSubscriptions: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToDebtTracker: () -> Unit,
    onSeedSampleData: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "⚡ Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    text = "Manage Subscriptions",
                    icon = Icons.Default.List,
                    onClick = onNavigateToSubscriptions,
                    modifier = Modifier.weight(1f)
                )
                
                ActionButton(
                    text = "Set Reminders",
                    icon = Icons.Default.Notifications,
                    onClick = onNavigateToReminders,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    text = "Debt Tracker",
                    icon = Icons.Default.TrendingUp,
                    onClick = onNavigateToDebtTracker,
                    modifier = Modifier.weight(1f)
                )
                
                ActionButton(
                    text = "Load Sample Data",
                    icon = Icons.Default.DataUsage,
                    onClick = onSeedSampleData,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EditBalanceDialog(
    currentBalance: Double,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var balanceText by remember { mutableStateOf(String.format("%.2f", currentBalance)) }
    var isError by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Edit Current Balance",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { 
                        balanceText = it
                        isError = it.toDoubleOrNull() == null
                    },
                    label = { Text("Current Balance") },
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = isError,
                    supportingText = if (isError) {
                        { Text("Please enter a valid amount") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            val newBalance = balanceText.toDoubleOrNull()
                            if (newBalance != null) {
                                onConfirm(newBalance)
                            }
                        },
                        enabled = !isError && balanceText.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun AddEssentialExpenseDialog(
    onConfirm: (String, ExpenseCategory, Double, Int?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.OTHER) }
    var amount by remember { mutableStateOf("") }
    var dueDay by remember { mutableStateOf("") }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF667eea),
                                Color(0xFF764ba2)
                            )
                        )
                    )
                    .padding(28.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Add Essential Expense",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Name field
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Expense Name", color = Color.White.copy(alpha = 0.8f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                            cursorColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Category dropdown
                    Box {
                        OutlinedTextField(
                            value = "${selectedCategory.iconEmoji} ${selectedCategory.displayName}",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Category", color = Color.White.copy(alpha = 0.8f)) },
                            trailingIcon = {
                                IconButton(onClick = { showCategoryDropdown = true }) {
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Category",
                                        tint = Color.White
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCategoryDropdown = true }
                        )
                        
                        DropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            ExpenseCategory.values().forEach { category ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(category.iconEmoji)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(category.displayName)
                                        }
                                    },
                                    onClick = {
                                        selectedCategory = category
                                        showCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Amount field
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { 
                            amount = it
                            isError = it.toDoubleOrNull() == null
                        },
                        label = { Text("Amount", color = Color.White.copy(alpha = 0.8f)) },
                        prefix = { Text("$", color = Color.White) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = isError,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                            cursorColor = Color.White,
                            errorBorderColor = Color(0xFFFF5722)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Due day field (optional)
                    OutlinedTextField(
                        value = dueDay,
                        onValueChange = { dueDay = it },
                        label = { Text("Due Day (1-31, optional)", color = Color.White.copy(alpha = 0.8f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                            cursorColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color.White.copy(alpha = 0.8f)
                            )
                        ) {
                            Text("Cancel")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = {
                                val parsedAmount = amount.toDoubleOrNull()
                                val parsedDueDay = dueDay.toIntOrNull()
                                
                                if (name.isNotBlank() && parsedAmount != null && parsedAmount > 0) {
                                    onConfirm(name, selectedCategory, parsedAmount, parsedDueDay)
                                }
                            },
                            enabled = name.isNotBlank() && !isError && amount.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF667eea)
                            )
                        ) {
                            Text("Add Expense", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteExpenseConfirmDialog(
    expense: EssentialExpenseEntity,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFF5722),
                                Color(0xFFE91E63)
                            )
                        )
                    )
                    .padding(28.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Delete Expense",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Are you sure you want to delete '${expense.name}'?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    
                    Text(
                        text = "This action cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color.White.copy(alpha = 0.8f)
                            )
                        ) {
                            Text("Cancel")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = onConfirm,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFFFF5722)
                            )
                        ) {
                            Text("Delete", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
