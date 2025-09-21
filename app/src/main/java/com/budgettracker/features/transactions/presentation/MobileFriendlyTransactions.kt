package com.budgettracker.features.transactions.presentation

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgettracker.core.data.repository.FirebaseRepository
import com.budgettracker.core.domain.model.Transaction
import com.budgettracker.core.domain.model.TransactionType
import com.budgettracker.ui.theme.Primary40
import java.text.SimpleDateFormat
import java.util.*

/**
 * Mobile-friendly modern transactions screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileFriendlyTransactions(
    onNavigateToAddTransaction: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { FirebaseRepository(context) }
    val transactions by repository.getTransactionsFlow().collectAsState(initial = emptyList())
    
    // Filter states
    var selectedFilter by remember { mutableStateOf("All") }
    val filterOptions = listOf("All", "Income", "Expenses", "This Month")
    
    val filteredTransactions = when (selectedFilter) {
        "Income" -> transactions.filter { it.type == TransactionType.INCOME }
        "Expenses" -> transactions.filter { it.type == TransactionType.EXPENSE }
        "This Month" -> transactions.filter { it.isCurrentMonth() }
        else -> transactions
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Transactions",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(onClick = { /* TODO: Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        },
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
            // Monthly Summary Header
            item {
                MonthlySummaryHeader(transactions = transactions)
            }
            
            // Filter Chips
            item {
                FilterChipsSection(
                    selectedFilter = selectedFilter,
                    filterOptions = filterOptions,
                    onFilterSelected = { selectedFilter = it }
                )
            }
            
            // Transaction List
            if (filteredTransactions.isEmpty()) {
                item {
                    EmptyStateCard(onAddTransaction = onNavigateToAddTransaction)
                }
            } else {
                items(filteredTransactions) { transaction ->
                    ModernTransactionCard(transaction = transaction)
                }
            }
        }
    }
}

@Composable
private fun MonthlySummaryHeader(transactions: List<Transaction>) {
    val monthlyIncome = transactions.filter { 
        it.type == TransactionType.INCOME && it.isCurrentMonth() 
    }.sumOf { it.amount }
    
    val monthlyExpenses = transactions.filter { 
        it.type == TransactionType.EXPENSE && it.isCurrentMonth() 
    }.sumOf { it.amount }
    
    val netAmount = monthlyIncome - monthlyExpenses
    
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
                        colors = listOf(
                            if (netAmount >= 0) Color(0xFF28a745) else Color(0xFFdc3545),
                            if (netAmount >= 0) Color(0xFF20c997) else Color(0xFFfd7e14)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "September 2025",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryItem(
                        label = "Income",
                        amount = "$${String.format("%.2f", monthlyIncome)}",
                        color = Color.White
                    )
                    
                    SummaryItem(
                        label = "Expenses", 
                        amount = "$${String.format("%.2f", monthlyExpenses)}",
                        color = Color.White
                    )
                    
                    SummaryItem(
                        label = "Net",
                        amount = if (netAmount >= 0) "+$${String.format("%.2f", netAmount)}" else "-$${String.format("%.2f", kotlin.math.abs(netAmount))}",
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    amount: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = amount,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = color.copy(alpha = 0.8f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipsSection(
    selectedFilter: String,
    filterOptions: List<String>,
    onFilterSelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filterOptions) { filter ->
            FilterChip(
                onClick = { onFilterSelected(filter) },
                label = { Text(filter) },
                selected = selectedFilter == filter,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Primary40,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun ModernTransactionCard(transaction: Transaction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = Color(android.graphics.Color.parseColor(transaction.category.color)).copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = transaction.category.icon,
                            fontSize = 20.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "${transaction.category.displayName} • ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(transaction.date)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (transaction.notes?.isNotBlank() == true) {
                        Text(
                            text = transaction.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = if (transaction.type == TransactionType.INCOME)
                        "+$${String.format("%.2f", transaction.amount)}"
                    else
                        "-$${String.format("%.2f", transaction.amount)}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (transaction.type == TransactionType.INCOME)
                            Color(0xFF28a745)
                        else
                            Color(0xFFdc3545)
                    )
                )
                
                if (transaction.isRecurring) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            text = "Recurring",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(4.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(onAddTransaction: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "💳",
                fontSize = 48.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No Transactions Yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Start tracking your finances by adding your first transaction",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Button(
                onClick = onAddTransaction,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Transaction")
            }
        }
    }
}

