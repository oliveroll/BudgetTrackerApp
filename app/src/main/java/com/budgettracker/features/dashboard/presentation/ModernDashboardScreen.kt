package com.budgettracker.features.dashboard.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgettracker.core.data.local.TransactionDataStore
import com.budgettracker.core.domain.model.Transaction
import com.budgettracker.core.domain.model.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernDashboardScreen(
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToBudget: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAddTransaction: () -> Unit = {}
) {
    var transactions by remember { mutableStateOf(TransactionDataStore.getTransactions()) }
    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var isDataLoaded by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        TransactionDataStore.initializeFromFirebase()
        transactions = TransactionDataStore.getTransactions()
        isDataLoaded = true
    }
    
    val filteredTransactions = remember(transactions, selectedMonth, selectedYear) {
        transactions.filter { transaction ->
            val calendar = Calendar.getInstance().apply { time = transaction.date }
            calendar.get(Calendar.MONTH) == selectedMonth && 
            calendar.get(Calendar.YEAR) == selectedYear
        }
    }
    
    val monthlyStats = remember(filteredTransactions) {
        calculateDashboardStats(filteredTransactions)
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
                    balance = monthlyStats.netBalance
                )
            }
            
            // Month Selector with Animation
            item {
                AnimatedMonthSelector(
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
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Income vs Expenses Card with Animation
            item {
                AnimatedIncomeExpensesCard(
                    stats = monthlyStats,
                    isVisible = isDataLoaded,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Monthly Totals Summary
            item {
                MonthlyTotalsCard(
                    stats = monthlyStats,
                    isVisible = isDataLoaded,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Spending by Category Chart
            item {
                SpendingByCategoryCard(
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
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
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
                                text = "Oliver Ollesch",
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
                            text = "Hardware Test Engineer at Ixana",
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
                                text = currencyFormat.format(balance),
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
            
            // Expenses Bar
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
                            text = "Expenses",
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
                                fraction = ((animatedExpenses / maxValue.toFloat()).coerceIn(0f, 1f))
                            )
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFdc3545),
                                        Color(0xFFff4d5a)
                                    )
                                )
                            )
                    )
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

private fun calculateDashboardStats(transactions: List<Transaction>): DashboardStats {
    val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val expenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    
    return DashboardStats(
        totalIncome = income,
        totalExpenses = expenses,
        netBalance = income - expenses,
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
    val netBalance: Double,
    val transactionCount: Int
)

