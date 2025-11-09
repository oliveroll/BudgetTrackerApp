package com.budgettracker.features.transactions.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DisplayMode
import androidx.hilt.navigation.compose.hiltViewModel
import com.budgettracker.core.data.local.TransactionDataStore
import com.budgettracker.core.domain.model.Transaction
import com.budgettracker.core.domain.model.TransactionCategory
import com.budgettracker.core.domain.model.TransactionType
import com.budgettracker.core.utils.rememberCurrencyFormatter
import com.budgettracker.core.utils.AnalyticsTracker
import com.budgettracker.features.settings.data.models.CategoryType
import com.budgettracker.features.settings.presentation.SettingsViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Add Transaction Screen with full functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onNavigateBack: () -> Unit = {},
    onSaveTransaction: (String, String, String, String, String) -> Unit = { _, _, _, _, _ -> },
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    // Track screen view
    LaunchedEffect(Unit) {
        AnalyticsTracker.trackScreenViewed("AddTransaction")
    }
    
    val currencyFormatter = rememberCurrencyFormatter()
    val scope = rememberCoroutineScope()
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TransactionCategory.MISCELLANEOUS) }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var notes by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) } // FIXED: Use LocalDate
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Get custom categories from settings
    val customCategories by settingsViewModel.categories.collectAsState()
    
    // FIXED: Convert LocalDate to millis for DatePicker (UTC midnight)
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Add Transaction",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (amount.isNotBlank() && description.isNotBlank()) {
                                val transaction = Transaction(
                                    id = UUID.randomUUID().toString(),
                                    userId = "demo_user",
                                    amount = amount.toDoubleOrNull() ?: 0.0,
                                    description = description,
                                    category = selectedCategory,
                                    type = selectedType,
                                    notes = notes.ifBlank { null },
                                    date = selectedDate
                                )
                                TransactionDataStore.addTransaction(transaction)
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Transaction Type Selection
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Transaction Type",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterChip(
                            onClick = { selectedType = TransactionType.INCOME },
                            label = { Text("Income") },
                            selected = selectedType == TransactionType.INCOME,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        
                        FilterChip(
                            onClick = { selectedType = TransactionType.EXPENSE },
                            label = { Text("Expense") },
                            selected = selectedType == TransactionType.EXPENSE,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Amount Input
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                leadingIcon = { Text(currencyFormatter.getSymbol()) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Description Input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Category Selection
            ExposedDropdownMenuBox(
                expanded = showCategoryDropdown,
                onExpandedChange = { showCategoryDropdown = !showCategoryDropdown }
            ) {
                OutlinedTextField(
                    value = selectedCategory.displayName,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
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
                    val relevantCustomCategories = customCategories.filter {
                        when (selectedType) {
                            TransactionType.INCOME -> it.type == CategoryType.INCOME
                            TransactionType.EXPENSE -> it.type == CategoryType.EXPENSE
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
                                    selectedCategory = TransactionCategory.entries.firstOrNull { 
                                        it.displayName.equals(customCat.name, ignoreCase = true) 
                                    } ?: TransactionCategory.MISCELLANEOUS
                                    description = customCat.name // Set description to custom category name
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
            }
            
            // Notes Input
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4
            )
            
            // Date Selection
            Card(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Transaction Date")
                    Row {
                        Text(
                            text = selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = "Select Date"
                        )
                    }
                }
            }
            
            // Save Button
            Button(
                onClick = {
                    if (amount.isNotBlank() && description.isNotBlank()) {
                        val transaction = Transaction(
                            id = UUID.randomUUID().toString(),
                            userId = "demo_user",
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            description = description,
                            category = selectedCategory,
                            type = selectedType,
                            notes = notes.ifBlank { null },
                            date = selectedDate
                        )
                        TransactionDataStore.addTransaction(transaction)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = amount.isNotBlank() && description.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Transaction")
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
                            // FIXED: Convert UTC millis directly to LocalDate
                            // No timezone shifts! DatePicker gives UTC midnight, we convert to LocalDate
                            selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
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
                        text = "Select Transaction Date",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            )
        }
    }
}
