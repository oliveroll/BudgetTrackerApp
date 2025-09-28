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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.budgettracker.core.domain.model.Transaction
import com.budgettracker.core.domain.model.TransactionType
import com.budgettracker.core.domain.model.TransactionCategory
import com.budgettracker.core.utils.RegionsBankPDFParser
import com.budgettracker.ui.theme.Primary40
import com.budgettracker.ui.theme.Secondary40
import java.text.SimpleDateFormat
import java.util.*

/**
 * Mobile-friendly modern transactions screen with PDF upload capability
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileFriendlyTransactions(
    onNavigateToAddTransaction: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pdfParser = remember { RegionsBankPDFParser(context) }
    
    // Use static demo data to prevent ANR issues (same as dashboard)
    var transactions by remember { mutableStateOf(getSampleTransactionsDetailed()) }
    
    // PDF upload state
    var showUploadDialog by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var processingMessage by remember { mutableStateOf("") }
    
    // PDF picker launcher
    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                isProcessing = true
                processingMessage = "Processing PDF..."
                
                try {
                    val result = pdfParser.parseRegionsBankStatement(uri)
                    if (result.isSuccess) {
                        val parsedTransactions = result.getOrNull() ?: emptyList()
                        if (parsedTransactions.isNotEmpty()) {
                            // Add parsed transactions to existing list
                            transactions = transactions + parsedTransactions
                            processingMessage = "Successfully parsed ${parsedTransactions.size} transactions!"
                        } else {
                            processingMessage = "No transactions found in PDF"
                        }
                    } else {
                        processingMessage = "Failed to parse PDF: ${result.exceptionOrNull()?.message}"
                    }
                } catch (e: Exception) {
                    processingMessage = "Error processing PDF: ${e.message}"
                }
                
                isProcessing = false
                showUploadDialog = false
            }
        }
    }
    
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
                    IconButton(onClick = { showUploadDialog = true }) {
                        Icon(Icons.Default.Upload, contentDescription = "Upload Statement")
                    }
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
    
    // PDF Upload Dialog
    if (showUploadDialog) {
        PDFUploadDialog(
            onDismiss = { showUploadDialog = false },
            onUploadPDF = { pdfLauncher.launch("application/pdf") },
            onManualAdd = { 
                showUploadDialog = false
                onNavigateToAddTransaction()
            }
        )
    }
    
    // Processing Dialog
    if (isProcessing) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text("Processing PDF")
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(processingMessage)
                }
            },
            confirmButton = { }
        )
    }
    
    // Show processing result
    if (processingMessage.isNotEmpty() && !isProcessing) {
        LaunchedEffect(processingMessage) {
            kotlinx.coroutines.delay(3000)
            processingMessage = ""
        }
        
        // Show snackbar or toast-like notification
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (processingMessage.contains("Successfully")) 
                    Color(0xFF28a745).copy(alpha = 0.1f) 
                else 
                    Color(0xFFdc3545).copy(alpha = 0.1f)
            )
        ) {
            Text(
                text = processingMessage,
                modifier = Modifier.padding(16.dp),
                color = if (processingMessage.contains("Successfully")) 
                    Color(0xFF28a745) 
                else 
                    Color(0xFFdc3545)
            )
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
                        amount = "$${String.format("%.0f", monthlyIncome)}",
                        color = Color.White
                    )
                    
                    SummaryItem(
                        label = "Expenses", 
                        amount = "$${String.format("%.0f", monthlyExpenses)}",
                        color = Color.White
                    )
                    
                    SummaryItem(
                        label = "Net",
                        amount = if (netAmount >= 0) "+$${String.format("%.0f", netAmount)}" else "-$${String.format("%.0f", kotlin.math.abs(netAmount))}",
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
                            fontSize = 18.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = "${transaction.category.displayName} • ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(transaction.date)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (transaction.notes?.isNotBlank() == true) {
                        Text(
                            text = transaction.notes!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Amount section - fixed width to prevent squeezing
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.widthIn(min = 90.dp)
            ) {
                Text(
                    text = if (transaction.type == TransactionType.INCOME)
                        "+$${String.format("%.0f", transaction.amount)}"
                    else
                        "-$${String.format("%.0f", transaction.amount)}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (transaction.type == TransactionType.INCOME)
                            Color(0xFF28a745)
                        else
                            Color(0xFFdc3545)
                    ),
                    maxLines = 1
                )
                
                if (transaction.isRecurring) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Recurring",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
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

@Composable
private fun PDFUploadDialog(
    onDismiss: () -> Unit,
    onUploadPDF: () -> Unit,
    onManualAdd: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "📄 Add Transactions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "How would you like to add transactions?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Upload PDF Option
                Card(
                    onClick = onUploadPDF,
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Primary40.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = "Upload PDF",
                            tint = Primary40,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = "Upload Bank Statement",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Parse PDF to extract transactions",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Manual Add Option
                Card(
                    onClick = onManualAdd,
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Secondary40.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Manual",
                            tint = Secondary40,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = "Add Transaction Manually",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Enter transaction details yourself",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Sample data function to prevent ANR (similar to dashboard)
private fun getSampleTransactionsDetailed(): List<Transaction> {
    return listOf(
        Transaction(
            id = "1",
            userId = "demo_user",
            amount = 2523.88,
            category = TransactionCategory.SALARY,
            type = TransactionType.INCOME,
            description = "Salary Deposit - Ixana Quasistatics",
            date = Date(),
            notes = "Bi-weekly salary deposit"
        ),
        Transaction(
            id = "2",
            userId = "demo_user",
            amount = 475.0,
            category = TransactionCategory.LOAN_PAYMENT,
            type = TransactionType.EXPENSE,
            description = "German Student Loan Payment",
            date = Date(System.currentTimeMillis() - 86400000),
            notes = "€450 monthly payment"
        ),
        Transaction(
            id = "3",
            userId = "demo_user",
            amount = 75.50,
            category = TransactionCategory.GROCERIES,
            type = TransactionType.EXPENSE,
            description = "Grocery Shopping - Walmart",
            date = Date(System.currentTimeMillis() - 3600000),
            notes = "Weekly groceries"
        ),
        Transaction(
            id = "4",
            userId = "demo_user",
            amount = 1200.0,
            category = TransactionCategory.RENT,
            type = TransactionType.EXPENSE,
            description = "Monthly Rent Payment",
            date = Date(System.currentTimeMillis() - 172800000),
            notes = "Apartment rent"
        ),
        Transaction(
            id = "5",
            userId = "demo_user",
            amount = 45.0,
            category = TransactionCategory.PHONE,
            type = TransactionType.EXPENSE,
            description = "Phone Plan - Verizon",
            date = Date(System.currentTimeMillis() - 259200000),
            notes = "Monthly phone bill"
        ),
        Transaction(
            id = "6",
            userId = "demo_user",
            amount = 150.0,
            category = TransactionCategory.INSURANCE,
            type = TransactionType.EXPENSE,
            description = "Car Insurance",
            date = Date(System.currentTimeMillis() - 345600000),
            notes = "Monthly car insurance"
        ),
        Transaction(
            id = "7",
            userId = "demo_user",
            amount = 85.25,
            category = TransactionCategory.DINING_OUT,
            type = TransactionType.EXPENSE,
            description = "Restaurant - Olive Garden",
            date = Date(System.currentTimeMillis() - 432000000),
            notes = "Dinner with friends"
        ),
        Transaction(
            id = "8",
            userId = "demo_user",
            amount = 25.0,
            category = TransactionCategory.TRANSPORTATION,
            type = TransactionType.EXPENSE,
            description = "Gas Station",
            date = Date(System.currentTimeMillis() - 518400000),
            notes = "Weekly gas fill-up"
        )
    )
}