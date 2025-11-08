package com.budgettracker.features.transactions.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.budgettracker.core.data.local.TransactionDataStore
import com.budgettracker.core.domain.model.Transaction
import com.budgettracker.core.domain.model.TransactionCategory
import com.budgettracker.core.domain.model.TransactionType
import com.budgettracker.core.utils.rememberCurrencyFormatter
import com.budgettracker.features.settings.data.models.CategoryType
import com.budgettracker.features.settings.presentation.SettingsViewModel
import com.budgettracker.ui.components.MonthSwitcher
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionListScreen(
onNavigateToAddTransaction: () -> Unit = {},
settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    var transactions by remember { mutableStateOf(emptyList<Transaction>()) }
    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    
    // Get custom categories from settings
    val customCategories by settingsViewModel.categories.collectAsState()
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    // Function to reload transactions (force refresh from Firebase)
    fun reloadTransactions() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                TransactionDataStore.initializeFromFirebase(forceReload = true)
                transactions = TransactionDataStore.getTransactions()
            } catch (e: Exception) {
                errorMessage = "Sync failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    LaunchedEffect(Unit) {
        // Load cached data immediately (no loading spinner)
        transactions = TransactionDataStore.getTransactions()
        
        // Sync from Firebase in background if needed
        scope.launch {
            try {
                TransactionDataStore.initializeFromFirebase(forceReload = false)
                transactions = TransactionDataStore.getTransactions()
            } catch (e: Exception) {
                errorMessage = "Background sync failed"
            }
        }
    }
    
    val filteredTransactions = remember(transactions, selectedMonth, selectedYear) {
        transactions.filter { transaction ->
            // FIXED: Use LocalDate methods instead of Calendar
            transaction.date.monthValue == (selectedMonth + 1) && 
            transaction.date.year == selectedYear
        }
    }
    
    val monthlyStats = remember(filteredTransactions) {
        calculateMonthlyStats(filteredTransactions)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "💳",
                            fontSize = 28.sp
                        )
                        Text(
                            text = "Transactions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { reloadTransactions() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reload Transactions",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddTransaction,
                icon = { 
                    Icon(
                        Icons.Default.Add, 
                        contentDescription = "Add Transaction"
                    ) 
                },
                text = { Text("Add") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }
            
            item {
                MonthSwitcher(
                    selectedMonth = selectedMonth,
                    selectedYear = selectedYear,
                    onMonthYearSelected = { month, year ->
                        selectedMonth = month
                        selectedYear = year
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Loading transactions...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else if (filteredTransactions.isEmpty()) {
                item {
                    EmptyStateCard(
                        onAddTransaction = onNavigateToAddTransaction,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${filteredTransactions.size} Transaction${if (filteredTransactions.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (transactions.size > filteredTransactions.size) {
                            Text(
                                text = "${transactions.size} total across all months",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                val groupedTransactions = filteredTransactions.groupBy { 
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it.date)
                }
                
                groupedTransactions.forEach { (date, dayTransactions) ->
                    item {
                        Text(
                            text = date,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                    
                    items(
                        items = dayTransactions,
                    key = { it.id }
                ) { transaction ->
                    SwipeToDeleteTransactionCard(
                        transaction = transaction,
                        customCategories = customCategories,
                        onDelete = { transactionToDelete = it },
                        onEdit = { updatedTransaction ->
                            // Update transaction in list
                            transactions = transactions.map {
                                if (it.id == updatedTransaction.id) updatedTransaction else it
                            }
                            // Update in backend
                            TransactionDataStore.updateTransaction(updatedTransaction)
                        },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .animateItem()
                    )
                }
                }
            }
        }
    }
    
    // Modern Delete Confirmation Dialog
    transactionToDelete?.let { transaction ->
        ModernDeleteConfirmationDialog(
            transaction = transaction,
            onDismiss = { transactionToDelete = null },
            onConfirm = {
                // Optimistic UI update
                transactions = transactions.filter { it.id != transaction.id }
                transactionToDelete = null
                
                // Delete from backend (async)
                TransactionDataStore.deleteTransaction(transaction.id)
            }
        )
    }
}

@Composable
private fun MonthlyStatsCard(
    stats: MonthlyStats,
    currentBalance: Double,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = rememberCurrencyFormatter()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Monthly Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatsChip(
                    label = "Income",
                    amount = currencyFormatter.format(stats.totalIncome),
                    icon = "📈",
                    color = Color(0xFF28a745),
                    modifier = Modifier.weight(1f)
                )
                
                StatsChip(
                    label = "Expenses",
                    amount = currencyFormatter.format(stats.totalExpenses),
                    icon = "📉",
                    color = Color(0xFFdc3545),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Current Balance",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${stats.transactionCount} transactions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = currencyFormatter.format(currentBalance),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (currentBalance >= 0) Color(0xFF28a745) else Color(0xFFdc3545),
                    fontSize = 28.sp
                )
            }
        }
    }
}

@Composable
private fun StatsChip(
    label: String,
    amount: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 18.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun TransactionCard(
    transaction: Transaction,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = rememberCurrencyFormatter()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (transaction.type == TransactionType.INCOME)
                            Color(0xFF28a745).copy(alpha = 0.15f)
                        else
                            Color(0xFFdc3545).copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = transaction.category.icon,
                    fontSize = 24.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 16.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = transaction.category.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    if (transaction.isRecurring) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = "Recurring",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                if (transaction.notes?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = transaction.notes!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.widthIn(min = 100.dp)
            ) {
                Text(
                    text = if (transaction.type == TransactionType.INCOME) 
                        "+${currencyFormatter.format(transaction.amount)}"
                    else 
                        "-${currencyFormatter.format(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.type == TransactionType.INCOME) 
                        Color(0xFF28a745) 
                    else 
                        Color(0xFFdc3545),
                    fontSize = 18.sp,
                    textAlign = TextAlign.End
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(transaction.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    onAddTransaction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "📊",
                fontSize = 64.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No Transactions This Month",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Add your first transaction to start tracking your finances",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onAddTransaction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Transaction",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun calculateMonthlyStats(transactions: List<Transaction>): MonthlyStats {
    val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val expenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    
    return MonthlyStats(
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

private data class MonthlyStats(
    val totalIncome: Double,
    val totalExpenses: Double,
    val netBalance: Double,
    val transactionCount: Int
)

/**
 * Swipeable Transaction Card with delete and edit gestures
 * Material 3 SwipeToDismissBox implementation with progressive background reveal
 * - Swipe Left (EndToStart): Red background → Delete
 * - Swipe Right (StartToEnd): Green background → Edit
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteTransactionCard(
    transaction: Transaction,
    customCategories: List<com.budgettracker.features.settings.data.models.CustomCategory>,
    onDelete: (Transaction) -> Unit,
    onEdit: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }
    
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    // Swipe left → Delete
                    onDelete(transaction)
                    false // Don't auto-dismiss
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    // Swipe right → Edit
                    showEditDialog = true
                    false // Don't auto-dismiss
                }
                else -> false
            }
        },
        positionalThreshold = { it * 0.4f } // Require 40% swipe to trigger
    )
    
    // Calculate swipe progress and direction
    val swipeProgress = dismissState.progress
    val targetValue = dismissState.targetValue
    val currentValue = dismissState.currentValue
    
    // Determine swipe direction
    val isSwipingLeft = targetValue == SwipeToDismissBoxValue.EndToStart
    val isSwipingRight = targetValue == SwipeToDismissBoxValue.StartToEnd
    
    // Reset swipe state when needed
    LaunchedEffect(currentValue) {
        if (currentValue != SwipeToDismissBoxValue.Settled) {
            dismissState.reset()
        }
    }
    
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            // Only show background when actively swiping
            val showBackground = swipeProgress > 0.05f
            
            if (showBackground) {
                // Choose background color based on swipe direction
                val backgroundColor = when {
                    isSwipingLeft -> MaterialTheme.colorScheme.error // Red for delete
                    isSwipingRight -> Color(0xFF28a745) // Green for edit
                    else -> Color.Transparent
                }
                
                // Progressive fade-in based on swipe progress
                val alpha = (swipeProgress * 2f).coerceIn(0f, 1f)
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = backgroundColor.copy(alpha = alpha),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 20.dp),
                    contentAlignment = if (isSwipingLeft) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    // Show appropriate icon based on direction
                    androidx.compose.animation.AnimatedVisibility(
                        visible = swipeProgress > 0.25f,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isSwipingLeft) {
                                // Delete icon and text
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.White.copy(alpha = alpha),
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Delete",
                                    color = Color.White.copy(alpha = alpha),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            } else if (isSwipingRight) {
                                // Edit icon and text
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = Color.White.copy(alpha = alpha),
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Edit",
                                    color = Color.White.copy(alpha = alpha),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        },
        enableDismissFromStartToEnd = true,  // Enable swipe right for edit
        enableDismissFromEndToStart = true   // Enable swipe left for delete
    ) {
        TransactionCard(
            transaction = transaction,
            modifier = Modifier.fillMaxWidth()
        )
    }
    
    // Modern Edit Transaction Dialog
    if (showEditDialog) {
        ModernEditTransactionDialog(
            transaction = transaction,
            customCategories = customCategories,
            onDismiss = { showEditDialog = false },
            onSave = { updatedTransaction ->
                // Pass updated transaction to parent
                onEdit(updatedTransaction)
                showEditDialog = false
            }
        )
    }
}

/**
 * Modern Edit Transaction Dialog
 * Full-featured form for editing transactions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernEditTransactionDialog(
    transaction: Transaction,
    customCategories: List<com.budgettracker.features.settings.data.models.CustomCategory>,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit
) {
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var description by remember { mutableStateOf(transaction.description) }
    var selectedCategory by remember { mutableStateOf(transaction.category) }
    var selectedType by remember { mutableStateOf(transaction.type) }
    var notes by remember { mutableStateOf(transaction.notes ?: "") }
    var selectedDate by remember { mutableStateOf(transaction.date) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // FIXED: Convert LocalDate to UTC millis for DatePicker
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(java.time.ZoneId.of("UTC")).toInstant().toEpochMilli()
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Edit Transaction",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Modify transaction details",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
                
                // Form content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Transaction Type Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterChip(
                            selected = selectedType == TransactionType.INCOME,
                            onClick = { selectedType = TransactionType.INCOME },
                            label = {
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    Text("💰 Income")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF28a745),
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = selectedType == TransactionType.EXPENSE,
                            onClick = { selectedType = TransactionType.EXPENSE },
                            label = {
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    Text("💸 Expense")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFdc3545),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    
                    // Amount Input
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount") },
                        leadingIcon = { 
                            Text(
                                "$",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    // Description Input
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        leadingIcon = {
                            Icon(Icons.Default.Description, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    // Category Dropdown
                    ExposedDropdownMenuBox(
                        expanded = showCategoryDropdown,
                        onExpandedChange = { showCategoryDropdown = !showCategoryDropdown }
                    ) {
                        OutlinedTextField(
                            value = "${selectedCategory.icon} ${selectedCategory.displayName}",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { 
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        ExposedDropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false }
                        ) {
                            // Get built-in categories
                            val builtInCategories = if (selectedType == TransactionType.INCOME) {
                                TransactionCategory.getIncomeCategories()
                            } else {
                                TransactionCategory.getExpenseCategories()
                            }
                            
                            // Display built-in categories
                            builtInCategories.forEach { category ->
                                DropdownMenuItem(
                                    text = {
                                        Row {
                                            Text(category.icon)
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
                            
                            // Add divider if there are custom categories
                            val relevantCustomCategories = customCategories.filter { category ->
                                when (selectedType) {
                                    TransactionType.INCOME -> category.type == CategoryType.INCOME
                                    TransactionType.EXPENSE -> category.type == CategoryType.EXPENSE
                                    else -> false
                                }
                            }
                            
                            if (relevantCustomCategories.isNotEmpty()) {
                                HorizontalDivider()
                                
                                // Display custom categories
                                relevantCustomCategories.forEach { customCat ->
                                    DropdownMenuItem(
                                        text = { 
                                            Row {
                                                Text("⭐") // Custom category indicator
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(customCat.name)
                                            }
                                        },
                                        onClick = {
                                            // Try to find matching built-in category or use MISCELLANEOUS
                                            selectedCategory = TransactionCategory.entries.firstOrNull { cat ->
                                                cat.displayName.equals(customCat.name, ignoreCase = true) 
                                            } ?: TransactionCategory.MISCELLANEOUS
                                            description = customCat.name // Set description to custom category name
                                            showCategoryDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    // Date Picker
                    OutlinedTextField(
                        value = SimpleDateFormat("MMM dd, yyyy - h:mm a", Locale.getDefault()).format(selectedDate),
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Date & Time") },
                        leadingIcon = {
                            Icon(Icons.Default.Event, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    // Notes Input
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (Optional)") },
                        leadingIcon = {
                            Icon(Icons.Default.Notes, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (amount.isNotBlank() && description.isNotBlank()) {
                                    val updatedTransaction = transaction.copy(
                                        amount = amount.toDoubleOrNull() ?: transaction.amount,
                                        description = description,
                                        category = selectedCategory,
                                        type = selectedType,
                                        notes = notes.ifBlank { null },
                                        date = selectedDate,
                                        updatedAt = Date()
                                    )
                                    onSave(updatedTransaction)
                                }
                            },
                            enabled = amount.isNotBlank() && description.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            // FIXED: Convert UTC millis directly to LocalDate (no timezone shifts!)
                            selectedDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.of("UTC"))
                                .toLocalDate()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = "Select Date",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            )
        }
    }
}

/**
 * Modern Delete Confirmation Dialog
 * Beautiful, clear confirmation with transaction preview
 */
@Composable
private fun ModernDeleteConfirmationDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val currencyFormatter = rememberCurrencyFormatter()
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header with red gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFdc3545),
                                    Color(0xFFc82333)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Delete Transaction?",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This action cannot be undone",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
                
                // Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Transaction Preview Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                // Description with icon
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    Text(
                                        text = transaction.category.icon,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = transaction.description,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                // Category
                                Text(
                                    text = transaction.category.displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                // Date
                                Text(
                                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(transaction.date),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            // Amount with background
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Text(
                                    text = currencyFormatter.format(transaction.amount),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                    
                    // Warning message
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "You're about to permanently delete this transaction",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                    
                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Medium)
                        }
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}