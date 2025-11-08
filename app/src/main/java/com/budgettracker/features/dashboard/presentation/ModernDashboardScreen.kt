package com.budgettracker.features.dashboard.presentation

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.budgettracker.core.data.local.TransactionDataStore
import com.budgettracker.core.domain.model.Transaction
import com.budgettracker.core.domain.model.TransactionType
import com.budgettracker.core.utils.rememberCurrencyFormatter
import com.budgettracker.features.budget.presentation.BudgetOverviewViewModel
import com.budgettracker.features.settings.presentation.SettingsViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

private const val TAG = "DashboardDebug"

private enum class ExpenseSegment {
    TRANSACTIONS, FIXED, SUBSCRIPTIONS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernDashboardScreen(
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToBudget: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToFinancialGoals: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAddTransaction: () -> Unit = {},
    budgetViewModel: BudgetOverviewViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    var transactions by remember { mutableStateOf(TransactionDataStore.getTransactions()) }
    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var isDataLoaded by remember { mutableStateOf(false) }
    
    val budgetUiState by budgetViewModel.uiState.collectAsState()
    val userSettings by settingsViewModel.userSettings.collectAsState()
    val employment by settingsViewModel.employment.collectAsState()
    
    LaunchedEffect(Unit) {
        // Initialize TransactionDataStore with budget repository for auto-pay
        TransactionDataStore.setBudgetRepository(budgetViewModel.getRepository())
        
        TransactionDataStore.initializeFromFirebase()
        transactions = TransactionDataStore.getTransactions()
        isDataLoaded = true
        
        // Recalculate balance after initial load
        budgetViewModel.recalculateBalance()
    }
    
    // Reload transactions when budget state changes (e.g., after adding/editing expenses)
    LaunchedEffect(budgetUiState.lastUpdated) {
        transactions = TransactionDataStore.getTransactions()
    }
    
    // Recalculate balance whenever transactions change
    LaunchedEffect(transactions.size) {
        if (isDataLoaded) {
            budgetViewModel.recalculateBalance()
        }
    }
    
    // Update budget data when month changes
    LaunchedEffect(selectedMonth, selectedYear) {
        budgetViewModel.setSelectedMonth(selectedMonth, selectedYear)
    }
    
    val filteredTransactions = remember(transactions, selectedMonth, selectedYear) {
        transactions.filter { transaction ->
            // FIXED: Use LocalDate methods instead of Calendar
            transaction.date.monthValue == (selectedMonth + 1) && 
            transaction.date.year == selectedYear
        }
    }
    
    // Calculate total expenses including transactions, ONLY FIXED expenses, and subscriptions
    val monthlyStats = remember(filteredTransactions, budgetUiState.essentialExpenses, budgetUiState.subscriptions, budgetUiState.isLoading) {
        // Don't calculate if ViewModel is still loading data
        if (budgetUiState.isLoading) {
            return@remember DashboardStats(
                totalIncome = 0.0,
                totalExpenses = 0.0,
                transactionExpenses = 0.0,
                fixedExpenses = 0.0,
                subscriptions = 0.0,
                netBalance = 0.0,
                transactionCount = 0
            )
        }
        
        Log.d(TAG, "=== DASHBOARD CALCULATION START ===")
        Log.d(TAG, "Selected Month: ${getMonthName(selectedMonth)} $selectedYear")
        Log.d(TAG, "Filtered Transactions Count: ${filteredTransactions.size}")
        
        // Log all transactions for this month
        filteredTransactions.forEach { txn ->
            val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(txn.date)
            Log.d(TAG, "  Transaction: ${txn.type} | ${txn.description} | $${txn.amount} | $dateStr | Category: ${txn.category.displayName}")
        }
        
        // Get ALL fixed expenses for display (both paid and unpaid)
        val allFixedExpenses = budgetUiState.essentialExpenses.filter { it.dueDay != null }
        val allFixedExpensesTotal = allFixedExpenses.sumOf { it.plannedAmount }
        
        // For calculation: only count UNPAID fixed expenses to prevent double-counting
        // (Paid fixed expenses are already reflected in transactions or balance)
        val unpaidFixedExpenses = allFixedExpenses.filter { !it.paid }
        val unpaidFixedExpensesTotal = unpaidFixedExpenses.sumOf { it.plannedAmount }
        
        Log.d(TAG, "\nFixed Expenses (All with dueDay != null):")
        Log.d(TAG, "  Total Fixed Expenses: ${allFixedExpenses.size} (Display: $$allFixedExpensesTotal)")
        Log.d(TAG, "  Unpaid Fixed Expenses: ${unpaidFixedExpenses.size} (Calculation: $$unpaidFixedExpensesTotal)")
        allFixedExpenses.forEach { exp ->
            val status = if (exp.paid) "PAID" else "UNPAID"
            Log.d(TAG, "    - ${exp.name}: $${exp.plannedAmount} | DueDay: ${exp.dueDay} | Status: $status")
        }
        
        val subscriptionsTotal = budgetUiState.subscriptions.sumOf { it.getMonthlyCost() }
        Log.d(TAG, "\nSubscriptions:")
        Log.d(TAG, "  Total Subscriptions: ${budgetUiState.subscriptions.size}")
        budgetUiState.subscriptions.forEach { sub ->
            Log.d(TAG, "    - ${sub.name}: $${sub.amount} | Frequency: ${sub.frequency}")
        }
        Log.d(TAG, "  Subscriptions Total: $$subscriptionsTotal")
        
        // Use allFixedExpensesTotal for DISPLAY, but unpaidFixedExpensesTotal for CALCULATION
        val stats = calculateDashboardStats(
            transactions = filteredTransactions,
            fixedExpensesCalculation = unpaidFixedExpensesTotal,
            fixedExpensesDisplay = allFixedExpensesTotal,
            subscriptionsTotal = subscriptionsTotal
        )
        
        Log.d(TAG, "\n=== FINAL CALCULATION ===")
        Log.d(TAG, "Income: $${stats.totalIncome}")
        Log.d(TAG, "Transaction Expenses: $${stats.transactionExpenses}")
        Log.d(TAG, "Fixed Expenses (Display ALL): $${stats.fixedExpenses}")
        Log.d(TAG, "Fixed Expenses (Calc Unpaid): $$unpaidFixedExpensesTotal")
        Log.d(TAG, "Subscriptions: $${stats.subscriptions}")
        Log.d(TAG, "Total Expenses: $${stats.totalExpenses}")
        Log.d(TAG, "Net Balance: $${stats.netBalance}")
        Log.d(TAG, "=== DASHBOARD CALCULATION END ===\n")
        
        stats
    }
    
    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Premium Welcome Header
            item {
                PremiumWelcomeHeader(
                    onNavigateToSettings = onNavigateToSettings,
                    balance = budgetUiState.currentBalance,
                    userDisplayName = userSettings?.displayName,
                    jobTitle = employment?.jobTitle,
                    employer = employment?.employer
                )
            }
            
            // Loading indicator for budget data
            if (budgetUiState.isLoading) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Loading budget data...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            
            // Unified Financial Overview Container
            item {
                UnifiedFinancialOverviewCard(
                    selectedMonth = selectedMonth,
                    selectedYear = selectedYear,
                    onMonthClick = { showMonthPicker = true },
                    onPreviousMonth = {
                        if (selectedMonth == 0) {
                            selectedMonth = 11
                            selectedYear -= 1
                        } else {
                            selectedMonth -= 1
                        }
                    },
                    onNextMonth = {
                        if (selectedMonth == 11) {
                            selectedMonth = 0
                            selectedYear += 1
                        } else {
                            selectedMonth += 1
                        }
                    },
                    stats = monthlyStats,
                    transactions = filteredTransactions,
                    isVisible = isDataLoaded,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Quick Actions
            item {
                QuickActionsGrid(
                    onNavigateToTransactions = onNavigateToTransactions,
                    onNavigateToBudget = onNavigateToBudget,
                    onNavigateToGoals = onNavigateToGoals,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
    
    if (showMonthPicker) {
        MonthPickerDialog(
            selectedMonth = selectedMonth,
            selectedYear = selectedYear,
            onMonthSelected = { month, year ->
                selectedMonth = month
                selectedYear = year
                showMonthPicker = false
            },
            onDismiss = { showMonthPicker = false }
        )
    }
}

@Composable
private fun PremiumWelcomeHeader(
    onNavigateToSettings: () -> Unit,
    balance: Double,
    userDisplayName: String?,
    jobTitle: String?,
    employer: String?,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = rememberCurrencyFormatter()
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good Morning"
            in 12..17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }
    
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        // Gradient Background Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF667eea),
                                Color(0xFF764ba2)
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Header Row with Settings
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = greeting,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = userDisplayName ?: "User",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            )
                        }
                        
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Job Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Work,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = buildString {
                                jobTitle?.let { append("$it") }
                                if (jobTitle != null && employer != null) {
                                    append(" at ")
                                }
                                employer?.let { append(it) }
                                if (jobTitle == null && employer == null) {
                                    append("Update your employment in Settings")
                                }
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Current Balance Display
                    Column {
                        Text(
                            text = "Current Balance",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = currencyFormatter.format(balance),
                                style = MaterialTheme.typography.displaySmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp
                            )
                            
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (balance >= 0) 
                                    Color(0xFF28a745).copy(alpha = 0.3f) 
                                else 
                                    Color(0xFFdc3545).copy(alpha = 0.3f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (balance >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(4.dp))
                                    
                                    Text(
                                        text = if (balance >= 0) "Positive" else "Negative",
                                        style = MaterialTheme.typography.labelLarge,
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
    }
}

@Composable
private fun UnifiedFinancialOverviewCard(
    selectedMonth: Int,
    selectedYear: Int,
    onMonthClick: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    stats: DashboardStats,
    transactions: List<Transaction>,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedIncome by animateFloatAsState(
        targetValue = if (isVisible) stats.totalIncome.toFloat() else 0f,
        animationSpec = tween(durationMillis = 1200, easing = EaseOutCubic),
        label = "income_anim"
    )
    
    val animatedExpenses by animateFloatAsState(
        targetValue = if (isVisible) stats.totalExpenses.toFloat() else 0f,
        animationSpec = tween(durationMillis = 1200, easing = EaseOutCubic),
        label = "expenses_anim"
    )
    
    val animatedProgress by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, easing = EaseOutCubic),
        label = "chart_anim"
    )
    
    val currencyFormatter = rememberCurrencyFormatter()
    val maxValue = maxOf(stats.totalIncome, stats.totalExpenses, 1.0)
    
    val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }
    val categoryData = remember(expenseTransactions) {
        expenseTransactions
            .groupBy { it.category }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Month Selector Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPreviousMonth,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous Month",
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                AnimatedContent(
                    targetState = "${getMonthName(selectedMonth)} $selectedYear",
                    transitionSpec = {
                        slideInVertically { it } + fadeIn() togetherWith
                        slideOutVertically { -it } + fadeOut()
                    },
                    label = "month_change"
                ) { monthText ->
                    TextButton(onClick = onMonthClick) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(10.dp))
                        
                        Text(
                            text = monthText,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp
                        )
                    }
                }
                
                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next Month",
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // Income vs Expenses Section
            Text(
                text = "Income vs Expenses",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Income Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF28a745))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Income",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    )
                }
                
                Text(
                    text = currencyFormatter.format(animatedIncome.toDouble()),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF28a745),
                    fontSize = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(
                            fraction = ((animatedIncome / maxValue.toFloat()).coerceIn(0f, 1f))
                        )
                        .clip(RoundedCornerShape(9.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF28a745),
                                    Color(0xFF34d058)
                                )
                            )
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Expenses Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFdc3545))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Expenses",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    )
                }
                
                Text(
                    text = currencyFormatter.format(animatedExpenses.toDouble()),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFdc3545),
                    fontSize = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Modern Interactive Expense Breakdown Bar
            var selectedSegment by remember { mutableStateOf<ExpenseSegment?>(null) }
            
            // For bar display, calculate total from ALL components to ensure fractions add up
            val displayTotal = stats.transactionExpenses + stats.fixedExpenses + stats.subscriptions
            if (displayTotal > 0) {
                val barFraction = ((displayTotal / maxValue).coerceIn(0.0, 1.0)).toFloat()
                val transFraction = (stats.transactionExpenses / displayTotal).toFloat()
                val fixedFraction = (stats.fixedExpenses / displayTotal).toFloat()
                val subsFraction = (stats.subscriptions / displayTotal).toFloat()
                
                Log.d(TAG, "Expense bar - Display Total: $$displayTotal")
                Log.d(TAG, "  Trans: $${stats.transactionExpenses} (${String.format("%.1f", transFraction * 100)}%)")
                Log.d(TAG, "  Fixed: $${stats.fixedExpenses} (${String.format("%.1f", fixedFraction * 100)}%)")
                Log.d(TAG, "  Subs: $${stats.subscriptions} (${String.format("%.1f", subsFraction * 100)}%)")
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = barFraction),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        // Transaction expenses segment (red)
                        if (stats.transactionExpenses > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(transFraction)
                                    .clickable {
                                        selectedSegment = ExpenseSegment.TRANSACTIONS
                                    }
                                    .background(Color(0xFFdc3545))
                            )
                        }
                        // Fixed expenses segment (orange)
                        if (stats.fixedExpenses > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(fixedFraction)
                                    .clickable {
                                        selectedSegment = ExpenseSegment.FIXED
                                    }
                                    .background(Color(0xFFfd7e14))
                            )
                        }
                        // Subscriptions segment (purple)
                        if (stats.subscriptions > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(subsFraction)
                                    .clickable {
                                        selectedSegment = ExpenseSegment.SUBSCRIPTIONS
                                    }
                                    .background(Color(0xFF6f42c1))
                            )
                        }
                    }
                }
                
                // Animated Detail Card
                AnimatedVisibility(
                    visible = selectedSegment != null,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    selectedSegment?.let { segment ->
                        Spacer(modifier = Modifier.height(12.dp))
                        ExpenseDetailCard(
                            segment = segment,
                            stats = stats,
                            onDismiss = { selectedSegment = null }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // Spending by Category Section
            Text(
                text = "Spending by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            if (categoryData.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📊",
                        fontSize = 40.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No expenses this month",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Category Bars
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    categoryData.forEach { (category, amount) ->
                        CompactCategoryItem(
                            category = category.displayName,
                            icon = category.icon,
                            amount = amount,
                            total = stats.totalExpenses,
                            color = getCategoryColor(categoryData.indexOf(category to amount)),
                            progress = animatedProgress
                        )
                    }
                }
            }
        }
    }
}

private data class SegmentInfo(
    val title: String,
    val amount: Double,
    val color: Color,
    val icon: String,
    val percentage: Double
)

@Composable
private fun ExpenseDetailCard(
    segment: ExpenseSegment,
    stats: DashboardStats,
    onDismiss: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val totalExp = stats.totalExpenses
    
    val info = when (segment) {
        ExpenseSegment.TRANSACTIONS -> {
            val percent = if (totalExp > 0) (stats.transactionExpenses / totalExp * 100) else 0.0
            SegmentInfo("Transactions", stats.transactionExpenses, Color(0xFFdc3545), "💳", percent)
        }
        ExpenseSegment.FIXED -> {
            val percent = if (totalExp > 0) (stats.fixedExpenses / totalExp * 100) else 0.0
            SegmentInfo("Fixed Expenses", stats.fixedExpenses, Color(0xFFfd7e14), "📌", percent)
        }
        ExpenseSegment.SUBSCRIPTIONS -> {
            val percent = if (totalExp > 0) (stats.subscriptions / totalExp * 100) else 0.0
            SegmentInfo("Subscriptions", stats.subscriptions, Color(0xFF6f42c1), "🔄", percent)
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDismiss() },
        colors = CardDefaults.cardColors(
            containerColor = info.color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = info.icon,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                
                Column {
                    Text(
                        text = info.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${String.format("%.1f", info.percentage)}% of expenses",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = currencyFormat.format(info.amount),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = info.color,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
private fun CompactCategoryItem(
    category: String,
    icon: String,
    amount: Double,
    total: Double,
    color: Color,
    progress: Float
) {
    val currencyFormatter = rememberCurrencyFormatter()
    val percentage = if (total > 0) (amount / total * 100).toInt() else 0
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = icon,
                    fontSize = 20.sp
                )
                
                Spacer(modifier = Modifier.width(10.dp))
                
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = currencyFormatter.format(amount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 16.sp
                )
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(
                        fraction = ((amount / total * progress).toFloat().coerceIn(0f, 1f))
                    )
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun AnimatedMonthSelector(
    selectedMonth: Int,
    selectedYear: Int,
    onMonthClick: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF4facfe),
                            Color(0xFF00f2fe)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPreviousMonth,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous Month",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }
                
                AnimatedContent(
                    targetState = "${getMonthName(selectedMonth)} $selectedYear",
                    transitionSpec = {
                        slideInVertically { it } + fadeIn() togetherWith
                        slideOutVertically { -it } + fadeOut()
                    },
                    label = "month_animation"
                ) { monthText ->
                    Card(
                        onClick = onMonthClick,
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Text(
                                text = monthText,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 20.sp
                            )
                        }
                    }
                }
                
                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next Month",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedIncomeExpensesCard(
    stats: DashboardStats,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedIncome by animateFloatAsState(
        targetValue = if (isVisible) stats.totalIncome.toFloat() else 0f,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "income_animation"
    )
    
    val animatedExpenses by animateFloatAsState(
        targetValue = if (isVisible) stats.totalExpenses.toFloat() else 0f,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "expenses_animation"
    )
    
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val maxValue = maxOf(stats.totalIncome, stats.totalExpenses, 1.0)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CompareArrows,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "Income vs Expenses",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 22.sp
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Income Bar
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF28a745))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Income",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Text(
                        text = currencyFormat.format(animatedIncome.toDouble()),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF28a745),
                        fontSize = 20.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(
                                fraction = ((animatedIncome / maxValue.toFloat()).coerceIn(0f, 1f))
                            )
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF28a745),
                                        Color(0xFF34d058)
                                    )
                                )
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Total Expenses Bar
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFdc3545))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Total Expenses",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Text(
                        text = currencyFormat.format(animatedExpenses.toDouble()),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFdc3545),
                        fontSize = 20.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Debug: Show breakdown values with percentages
                val totalExp = stats.totalExpenses
                val transPercent = if (totalExp > 0) (stats.transactionExpenses / totalExp * 100) else 0.0
                val fixedPercent = if (totalExp > 0) (stats.fixedExpenses / totalExp * 100) else 0.0
                val subsPercent = if (totalExp > 0) (stats.subscriptions / totalExp * 100) else 0.0
                
                Column(modifier = Modifier.padding(bottom = 4.dp)) {
                    Text(
                        text = "Breakdown: Trans=$${String.format("%.2f", stats.transactionExpenses)} (${String.format("%.1f", transPercent)}%) | Fixed=$${String.format("%.2f", stats.fixedExpenses)} (${String.format("%.1f", fixedPercent)}%) | Subs=$${String.format("%.2f", stats.subscriptions)} (${String.format("%.1f", subsPercent)}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Total: $${String.format("%.2f", totalExp)} | Bar fraction: ${String.format("%.2f", (totalExp / maxValue))} | MaxValue: $${String.format("%.2f", maxValue)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                
                // Stacked bar showing expense breakdown
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)  // Made taller for better visibility
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    // Calculate proportions for stacked segments
                    // For bar display, use ALL values (not the calculation total)
                    val displayTotal = stats.transactionExpenses + stats.fixedExpenses + stats.subscriptions
                    if (displayTotal > 0) {
                        val barFraction = ((displayTotal / maxValue).coerceIn(0.0, 1.0)).toFloat()
                        
                        // Calculate each segment as a fraction of display total
                        val transFraction = (stats.transactionExpenses / displayTotal).toFloat()
                        val fixedFraction = (stats.fixedExpenses / displayTotal).toFloat()
                        val subsFraction = (stats.subscriptions / displayTotal).toFloat()
                        
                        Log.d(TAG, "Bar display total: $$displayTotal")
                        Log.d(TAG, "Bar amounts: Trans=$${stats.transactionExpenses}, Fixed=$${stats.fixedExpenses}, Subs=$${stats.subscriptions}")
                        Log.d(TAG, "Bar fractions: Trans=${String.format("%.1f", transFraction * 100)}%, Fixed=${String.format("%.1f", fixedFraction * 100)}%, Subs=${String.format("%.1f", subsFraction * 100)}%")
                        Log.d(TAG, "Total fraction: ${String.format("%.1f", (transFraction + fixedFraction + subsFraction) * 100)}%")
                        
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = barFraction),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            // Transaction expenses (dark red)
                            if (stats.transactionExpenses > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(transFraction)
                                        .background(Color(0xFFdc3545))
                                )
                            }
                            // Fixed expenses (orange)
                            if (stats.fixedExpenses > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(fixedFraction)
                                        .background(Color(0xFFfd7e14))
                                )
                            }
                            // Subscriptions (purple)
                            if (stats.subscriptions > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(subsFraction)
                                        .background(Color(0xFF6f42c1))
                                )
                            }
                        }
                    } else {
                        // No expenses - show empty bar
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.05f)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
                
                // Expense breakdown legend - only show if there are expenses
                if (stats.totalExpenses > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Transaction expenses
                        if (stats.transactionExpenses > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFdc3545))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Transactions",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = currencyFormat.format(stats.transactionExpenses),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        // Fixed expenses
                        if (stats.fixedExpenses > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFfd7e14))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Fixed Expenses",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = currencyFormat.format(stats.fixedExpenses),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        // Subscriptions
                        if (stats.subscriptions > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF6f42c1))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Subscriptions",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = currencyFormat.format(stats.subscriptions),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyTotalsCard(
    stats: DashboardStats,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedIncome by animateFloatAsState(
        targetValue = if (isVisible) stats.totalIncome.toFloat() else 0f,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "total_income"
    )
    
    val animatedExpenses by animateFloatAsState(
        targetValue = if (isVisible) stats.totalExpenses.toFloat() else 0f,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "total_expenses"
    )
    
    val animatedBalance by animateFloatAsState(
        targetValue = if (isVisible) stats.netBalance.toFloat() else 0f,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "balance"
    )
    
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TotalCard(
            title = "Income",
            amount = currencyFormat.format(animatedIncome.toDouble()),
            icon = "💰",
            color = Color(0xFF28a745),
            modifier = Modifier.weight(1f)
        )
        
        TotalCard(
            title = "Expenses",
            amount = currencyFormat.format(animatedExpenses.toDouble()),
            icon = "💸",
            color = Color(0xFFdc3545),
            modifier = Modifier.weight(1f)
        )
        
        TotalCard(
            title = "Balance",
            amount = currencyFormat.format(animatedBalance.toDouble()),
            icon = if (stats.netBalance >= 0) "✅" else "⚠️",
            color = if (stats.netBalance >= 0) Color(0xFF28a745) else Color(0xFFdc3545),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TotalCard(
    title: String,
    amount: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.shadow(6.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.15f),
                            color.copy(alpha = 0.05f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = icon,
                        fontSize = 28.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = amount,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun SpendingByCategoryCard(
    transactions: List<Transaction>,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }
    
    val categoryData = remember(expenseTransactions) {
        expenseTransactions
            .groupBy { it.category }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
            .take(6)
    }
    
    val animatedProgress by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "chart_animation"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "Spending by Category",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 22.sp
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (categoryData.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📊",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No expenses this month",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Donut Chart
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DonutChart(
                        data = categoryData,
                        progress = animatedProgress,
                        modifier = Modifier.size(220.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Category Legend
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    categoryData.forEach { (category, amount) ->
                        CategoryLegendItem(
                            category = category.displayName,
                            icon = category.icon,
                            amount = amount,
                            color = getCategoryColor(categoryData.indexOf(category to amount))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    data: List<Pair<com.budgettracker.core.domain.model.TransactionCategory, Double>>,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it.second }
    val colors = remember { data.mapIndexed { index, _ -> getCategoryColor(index) } }
    
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = minOf(centerX, centerY) * 0.9f
        val innerRadius = radius * 0.6f
        
        var startAngle = -90f
        
        data.forEachIndexed { index, (_, amount) ->
            val sweepAngle = ((amount / total) * 360f * progress).toFloat()
            
            // Draw arc segment
            drawArc(
                color = colors[index],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = androidx.compose.ui.geometry.Offset(
                    centerX - radius,
                    centerY - radius
                ),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )
            
            startAngle += sweepAngle
        }
        
        // Draw inner circle to create donut effect
        drawCircle(
            color = androidx.compose.ui.graphics.Color.White,
            radius = innerRadius,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY)
        )
    }
}

@Composable
private fun CategoryLegendItem(
    category: String,
    icon: String,
    amount: Double,
    color: Color
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = icon,
                fontSize = 20.sp
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = category,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
        
        Text(
            text = currencyFormat.format(amount),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.widthIn(min = 90.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun QuickActionsGrid(
    onNavigateToTransactions: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToGoals: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = "Transactions",
                icon = Icons.Default.Receipt,
                color = Color(0xFF007bff),
                onClick = onNavigateToTransactions,
                modifier = Modifier.weight(1f)
            )
            
            QuickActionCard(
                title = "Budget",
                icon = Icons.Default.AccountBalance,
                color = Color(0xFF28a745),
                onClick = onNavigateToBudget,
                modifier = Modifier.weight(1f)
            )
            
            QuickActionCard(
                title = "Goals",
                icon = Icons.Default.Savings,
                color = Color(0xFF6f42c1),
                onClick = onNavigateToGoals,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.shadow(2.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = color,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthPickerDialog(
    selectedMonth: Int,
    selectedYear: Int,
    onMonthSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var tempMonth by remember { mutableStateOf(selectedMonth) }
    var tempYear by remember { mutableStateOf(selectedYear) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Month",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { tempYear -= 1 }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Year")
                    }
                    
                    Text(
                        text = tempYear.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = { tempYear += 1 }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Year")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (0..11).chunked(3).forEach { monthRow ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            monthRow.forEach { month ->
                                FilterChip(
                                    selected = tempMonth == month,
                                    onClick = { tempMonth = month },
                                    label = { 
                                        Text(
                                            text = getMonthName(month).substring(0, 3),
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Center
                                        ) 
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onMonthSelected(tempMonth, tempYear) }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getCategoryColor(index: Int): Color {
    val colors = listOf(
        Color(0xFF007bff), // Blue
        Color(0xFF28a745), // Green
        Color(0xFFffc107), // Yellow
        Color(0xFFdc3545), // Red
        Color(0xFF6f42c1), // Purple
        Color(0xFF17a2b8), // Cyan
        Color(0xFFfd7e14), // Orange
        Color(0xFFe83e8c)  // Pink
    )
    return colors[index % colors.size]
}

private fun calculateDashboardStats(
    transactions: List<Transaction>,
    fixedExpensesCalculation: Double = 0.0, // Unpaid only - for calculation
    fixedExpensesDisplay: Double = 0.0,     // All (paid + unpaid) - for UI display
    subscriptionsTotal: Double = 0.0
): DashboardStats {
    val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val transactionExpenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    
    // Total expenses: transaction expenses + UNPAID fixed expenses + subscriptions
    // We only add unpaid fixed expenses to prevent double-counting with transactions
    val totalExpenses = transactionExpenses + fixedExpensesCalculation + subscriptionsTotal
    
    return DashboardStats(
        totalIncome = income,
        totalExpenses = totalExpenses,
        transactionExpenses = transactionExpenses,
        fixedExpenses = fixedExpensesDisplay, // Display ALL fixed expenses in UI
        subscriptions = subscriptionsTotal,
        netBalance = income - totalExpenses,
        transactionCount = transactions.size
    )
}

private fun getMonthName(month: Int): String {
    return when (month) {
        Calendar.JANUARY -> "January"
        Calendar.FEBRUARY -> "February"
        Calendar.MARCH -> "March"
        Calendar.APRIL -> "April"
        Calendar.MAY -> "May"
        Calendar.JUNE -> "June"
        Calendar.JULY -> "July"
        Calendar.AUGUST -> "August"
        Calendar.SEPTEMBER -> "September"
        Calendar.OCTOBER -> "October"
        Calendar.NOVEMBER -> "November"
        Calendar.DECEMBER -> "December"
        else -> "Unknown"
    }
}

private data class DashboardStats(
    val totalIncome: Double,
    val totalExpenses: Double,
    val transactionExpenses: Double,
    val fixedExpenses: Double,
    val subscriptions: Double,
    val netBalance: Double,
    val transactionCount: Int
)

