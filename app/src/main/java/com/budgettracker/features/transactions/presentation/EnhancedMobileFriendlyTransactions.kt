package com.budgettracker.features.transactions.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.abs
import com.budgettracker.core.domain.model.Transaction
import com.budgettracker.core.domain.model.TransactionType
import com.budgettracker.core.domain.model.TransactionCategory
import com.budgettracker.core.data.local.TransactionDataStore
import com.budgettracker.core.utils.FixedRegionsBankPDFParser
import com.budgettracker.ui.theme.Primary40
import com.budgettracker.ui.theme.Secondary40
import java.text.SimpleDateFormat
import java.util.*
import java.security.MessageDigest

/**
 * Enhanced mobile-friendly transactions screen with month selection and swipe actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedMobileFriendlyTransactions(
    onNavigateToAddTransaction: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pdfParser = remember { FixedRegionsBankPDFParser(context) }
    // Use singleton data store for persistence across tab navigation
    var transactions by remember { mutableStateOf(TransactionDataStore.getTransactions()) }
    
    // Initialize Firebase data on first load
    LaunchedEffect(Unit) {
        TransactionDataStore.initializeFromFirebase()
        transactions = TransactionDataStore.getTransactions()
    }
    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var showMonthPicker by remember { mutableStateOf(false) }
    
    // PDF upload state
    var showUploadDialog by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var processingMessage by remember { mutableStateOf("") }
    
    // Edit transaction state
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    
    // Delete confirmation state
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }
    
    // PDF picker launcher with duplicate prevention
    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                isProcessing = true
                processingMessage = "Processing Regions Bank statement..."
                
                try {
                    // Create document hash to prevent duplicate parsing
                    val documentHash = generateDocumentHash(uri.toString())
                    
                    // Check if already parsed
                    if (TransactionDataStore.isDocumentParsed(documentHash)) {
                        processingMessage = "This document has already been parsed!"
                        isProcessing = false
                        showUploadDialog = false
                        return@launch
                    }
                    
                    val result = pdfParser.parseRegionsBankStatement(uri)
                    if (result.isSuccess) {
                        val parsedTransactions = result.getOrNull() ?: emptyList()
                        if (parsedTransactions.isNotEmpty()) {
                            // Add to persistent data store
                            val addedCount = TransactionDataStore.addTransactions(parsedTransactions, documentHash)
                            
                            if (addedCount > 0) {
                                // Update local state to trigger UI refresh
                                transactions = TransactionDataStore.getTransactions()
                                processingMessage = "Successfully added $addedCount new transactions! (${parsedTransactions.size - addedCount} duplicates skipped)"
                            } else {
                                processingMessage = "All ${parsedTransactions.size} transactions were duplicates and skipped"
                            }
                        } else {
                            processingMessage = "No transactions found in the statement"
                        }
                    } else {
                        processingMessage = "Failed to parse statement: ${result.exceptionOrNull()?.message}"
                    }
                } catch (e: Exception) {
                    processingMessage = "Error processing PDF: ${e.message}"
                }
                
                isProcessing = false
                showUploadDialog = false
            }
        }
    }
    
    // Filter transactions by selected month/year (already sorted by date)
    val filteredTransactions = remember(selectedMonth, selectedYear, transactions) {
        TransactionDataStore.getTransactionsForMonth(selectedMonth, selectedYear)
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
            // Month Selector Header
            item {
                MonthSelectorHeader(
                    selectedMonth = selectedMonth,
                    selectedYear = selectedYear,
                    onMonthPickerClick = { showMonthPicker = true }
                )
            }
            
            // Monthly Summary for Selected Month
            item {
                EnhancedMonthlySummaryHeader(
                    transactions = filteredTransactions,
                    month = selectedMonth,
                    year = selectedYear
                )
            }
            
            // Transaction List with Swipe Actions
            if (filteredTransactions.isEmpty()) {
                item {
                    EmptyMonthStateCard(
                        month = selectedMonth,
                        year = selectedYear,
                        onAddTransaction = onNavigateToAddTransaction
                    )
                }
            } else {
                items(filteredTransactions, key = { it.id }) { transaction ->
                    SwipeableTransactionCard(
                        transaction = transaction,
                        onEdit = { editingTransaction = it },
                        onDelete = { transactionToDelete = it }
                    )
                }
            }
        }
    }
    
    // Month Picker Dialog
    if (showMonthPicker) {
        MonthPickerDialog(
            currentMonth = selectedMonth,
            currentYear = selectedYear,
            onDismiss = { showMonthPicker = false },
            onMonthSelected = { month, year ->
                selectedMonth = month
                selectedYear = year
                showMonthPicker = false
            }
        )
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
            title = { Text("Processing Regions Bank Statement") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(processingMessage)
                }
            },
            confirmButton = { }
        )
    }
    
    // Edit Transaction Dialog
    editingTransaction?.let { transaction ->
        EditTransactionDialog(
            transaction = transaction,
            onDismiss = { editingTransaction = null },
            onSave = { updatedTransaction ->
                TransactionDataStore.updateTransaction(updatedTransaction)
                transactions = TransactionDataStore.getTransactions()
                editingTransaction = null
            }
        )
    }
    
    // Delete Confirmation Dialog
    transactionToDelete?.let { transaction ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Delete Transaction") },
            text = { 
                Text("Are you sure you want to delete \"${transaction.description}\" for $${String.format("%.2f", transaction.amount)}?") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        TransactionDataStore.deleteTransaction(transaction.id)
                        transactions = TransactionDataStore.getTransactions()
                        transactionToDelete = null
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MonthSelectorHeader(
    selectedMonth: Int,
    selectedYear: Int,
    onMonthPickerClick: () -> Unit
) {
    Card(
        onClick = onMonthPickerClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Primary40.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Select Month",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${getMonthName(selectedMonth)} $selectedYear",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Primary40
                )
            }
            
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = "Select Month",
                tint = Primary40,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun EnhancedMonthlySummaryHeader(
    transactions: List<Transaction>,
    month: Int,
    year: Int
) {
    val monthlyIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val monthlyExpenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
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
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${getMonthName(month)} $year Overview",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "${transactions.size} transactions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.widthIn(min = 80.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    EnhancedSummaryItem(
                        label = "Income",
                        amount = "$${String.format("%.0f", monthlyIncome)}",
                        color = Color.White,
                        icon = Icons.Default.TrendingUp
                    )
                    
                    EnhancedSummaryItem(
                        label = "Expenses", 
                        amount = "$${String.format("%.0f", monthlyExpenses)}",
                        color = Color.White,
                        icon = Icons.Default.TrendingDown
                    )
                    
                    EnhancedSummaryItem(
                        label = "Net",
                        amount = if (netAmount >= 0) "+$${String.format("%.0f", netAmount)}" else "-$${String.format("%.0f", kotlin.math.abs(netAmount))}",
                        color = Color.White,
                        icon = if (netAmount >= 0) Icons.Default.Savings else Icons.Default.Warning
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedSummaryItem(
    label: String,
    amount: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.widthIn(min = 100.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(
            text = amount,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SwipeableTransactionCard(
    transaction: Transaction,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit
) {
    var showSwipeActions by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    
    if (showSwipeActions) {
        // Show action buttons instead of swipe
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Edit Button
                Card(
                    onClick = { 
                        onEdit(transaction)
                        showSwipeActions = false
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF007bff)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Edit",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Delete Button
                Card(
                    onClick = { 
                        onDelete(transaction)
                        showSwipeActions = false
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFdc3545)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Delete",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Cancel Button
                TextButton(onClick = { showSwipeActions = false }) {
                    Text("Cancel")
                }
            }
        }
    } else {
        // Regular transaction card with long press to show actions
        Card(
            onClick = { showSwipeActions = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            ModernTransactionCardContent(transaction = transaction)
        }
    }
}

@Composable
private fun ModernTransactionCardContent(transaction: Transaction) {
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
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = transaction.category.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Text(
                            text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(transaction.date),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    if (transaction.notes?.isNotBlank() == true) {
                        Text(
                            text = transaction.notes!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
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

@Composable
private fun MonthPickerDialog(
    currentMonth: Int,
    currentYear: Int,
    onDismiss: () -> Unit,
    onMonthSelected: (month: Int, year: Int) -> Unit
) {
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    
    var selectedMonth by remember { mutableStateOf(currentMonth) }
    var selectedYear by remember { mutableStateOf(currentYear) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "📅 Select Month",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                // Year selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedYear-- }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Year")
                    }
                    
                    Text(
                        text = selectedYear.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = { selectedYear++ }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Year")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Month grid
                LazyColumn {
                    items(months.chunked(3)) { monthRow ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            monthRow.forEachIndexed { index, monthName ->
                                val monthIndex = months.indexOf(monthName)
                                FilterChip(
                                    onClick = { selectedMonth = monthIndex },
                                    label = { Text(monthName.take(3)) },
                                    selected = selectedMonth == monthIndex,
                                    modifier = Modifier.weight(1f).padding(2.dp)
                                )
                            }
                            
                            // Fill remaining space if less than 3 items
                            repeat(3 - monthRow.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onMonthSelected(selectedMonth, selectedYear) }
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTransactionDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit
) {
    var description by remember { mutableStateOf(transaction.description) }
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var notes by remember { mutableStateOf(transaction.notes ?: "") }
    var selectedCategory by remember { mutableStateOf(transaction.category) }
    var selectedType by remember { mutableStateOf(transaction.type) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showTypeDropdown by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "✏️ Edit Transaction",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount") },
                        modifier = Modifier.weight(1f),
                        prefix = { Text("$") }
                    )
                    
                    // Transaction Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = showTypeDropdown,
                        onExpandedChange = { showTypeDropdown = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedType.name,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeDropdown) },
                            modifier = Modifier.menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = showTypeDropdown,
                            onDismissRequest = { showTypeDropdown = false }
                        ) {
                            TransactionType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name) },
                                    onClick = {
                                        selectedType = type
                                        showTypeDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = showCategoryDropdown,
                    onExpandedChange = { showCategoryDropdown = it }
                ) {
                    OutlinedTextField(
                        value = "${selectedCategory.icon} ${selectedCategory.displayName}",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        TransactionCategory.values().forEach { category ->
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = category.icon,
                                            modifier = Modifier.width(24.dp)
                                        )
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
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updatedTransaction = transaction.copy(
                        description = description,
                        amount = amount.toDoubleOrNull() ?: transaction.amount,
                        category = selectedCategory,
                        type = selectedType,
                        notes = notes.ifBlank { null }
                    )
                    onSave(updatedTransaction)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EmptyMonthStateCard(
    month: Int,
    year: Int,
    onAddTransaction: () -> Unit
) {
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
                text = "📊",
                fontSize = 48.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No Transactions in ${getMonthName(month)}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "No transactions found for ${getMonthName(month)} $year. Add your first transaction or upload a bank statement.",
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
                
                // Upload Regions Bank Statement
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
                                text = "Upload Regions Bank Statement",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Automatically extract transactions from PDF",
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

// Utility functions
private fun getMonthName(month: Int): String {
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    return months.getOrNull(month) ?: "Unknown"
}

// Generate document hash to prevent duplicate parsing
private fun generateDocumentHash(uriString: String): String {
    return try {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(uriString.toByteArray())
        hashBytes.joinToString("") { "%02x".format(it) }.take(16)
    } catch (e: Exception) {
        "unknown_${System.currentTimeMillis()}"
    }
}


