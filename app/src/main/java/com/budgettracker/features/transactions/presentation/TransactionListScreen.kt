package com.budgettracker.features.transactions.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgettracker.core.data.local.TransactionDataStore
import com.budgettracker.core.domain.model.Transaction
import com.budgettracker.core.domain.model.TransactionType
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionListScreen(
    onNavigateToAddTransaction: () -> Unit = {}
) {
    var transactions by remember { mutableStateOf(TransactionDataStore.getTransactions()) }
    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        TransactionDataStore.initializeFromFirebase()
        transactions = TransactionDataStore.getTransactions()
    }
    
    val filteredTransactions = remember(transactions, selectedMonth, selectedYear) {
        transactions.filter { transaction ->
            val calendar = Calendar.getInstance().apply { time = transaction.date }
            calendar.get(Calendar.MONTH) == selectedMonth && 
            calendar.get(Calendar.YEAR) == selectedYear
        }
    }
    
    val monthlyStats = remember(filteredTransactions) {
        calculateMonthlyStats(filteredTransactions)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Transactions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
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
                MonthNavigationCard(
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
            
            item {
                MonthlyStatsCard(
                    stats = monthlyStats,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            if (filteredTransactions.isEmpty()) {
                item {
                    EmptyStateCard(
                        onAddTransaction = onNavigateToAddTransaction,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                item {
                    Text(
                        text = "${filteredTransactions.size} Transaction${if (filteredTransactions.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
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
                            onDelete = { transactionToDelete = it },
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .animateItem()
                        )
                    }
                }
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
    
    // Delete Confirmation Dialog
    transactionToDelete?.let { transaction ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = "Delete Transaction?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text("Are you sure you want to delete this transaction?")
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = transaction.description,
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${transaction.category.icon} ${transaction.category.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = NumberFormat.getCurrencyInstance().format(transaction.amount),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This action cannot be undone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Optimistic UI update
                        val deletedTransaction = transaction
                        transactions = transactions.filter { it.id != deletedTransaction.id }
                        transactionToDelete = null
                        
                        // Delete from backend (async)
                        TransactionDataStore.deleteTransaction(deletedTransaction.id)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { transactionToDelete = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MonthNavigationCard(
    selectedMonth: Int,
    selectedYear: Int,
    onMonthClick: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPreviousMonth,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous Month",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Card(
                onClick = onMonthClick,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "${getMonthName(selectedMonth)} $selectedYear",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            IconButton(
                onClick = onNextMonth,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next Month",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun MonthlyStatsCard(
    stats: MonthlyStats,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    
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
                    amount = currencyFormat.format(stats.totalIncome),
                    icon = "📈",
                    color = Color(0xFF28a745),
                    modifier = Modifier.weight(1f)
                )
                
                StatsChip(
                    label = "Expenses",
                    amount = currencyFormat.format(stats.totalExpenses),
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
                        text = "Net Balance",
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
                    text = currencyFormat.format(stats.netBalance),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (stats.netBalance >= 0) Color(0xFF28a745) else Color(0xFFdc3545),
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
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    
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
                        "+${currencyFormat.format(transaction.amount)}"
                    else 
                        "-${currencyFormat.format(transaction.amount)}",
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
                                            text = getMonthName(month),
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
    onDelete: (Transaction) -> Unit,
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
    
    // Edit Dialog (placeholder - can be replaced with navigation to edit screen)
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = "Edit Transaction",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text("Edit functionality will be implemented here.")
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = transaction.description,
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${transaction.category.icon} ${transaction.category.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = NumberFormat.getCurrencyInstance().format(transaction.amount),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showEditDialog = false }
                ) {
                    Text("Close")
                }
            }
        )
    }
}