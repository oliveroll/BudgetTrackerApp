package com.budgettracker.features.budget.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.budgettracker.core.data.repository.FirebaseRepository
import com.budgettracker.core.domain.model.*
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Enhanced Budget screen with table design matching HTML
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedBudgetScreen() {
    val context = LocalContext.current
    val repository = remember { FirebaseRepository(context) }
    val scope = rememberCoroutineScope()
    
    // Real-time data from Firebase
    val incomeSources by repository.getIncomeSourcesFlow().collectAsState(initial = emptyList())
    val fixedExpenses by repository.getFixedExpensesFlow().collectAsState(initial = emptyList())
    val variableExpenses by repository.getVariableExpenseCategoriesFlow().collectAsState(initial = emptyList())
    
    // Auto-initialize data when screen loads
    LaunchedEffect(Unit) {
        // This will trigger the default data creation in the repository flows
        // The flows will automatically create default data if collections are empty
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "💸 Budget Overview",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Income Breakdown
            item {
                IncomeBreakdownCard(
                    incomeSources = incomeSources,
                    onEditIncome = { income ->
                        scope.launch {
                            repository.saveIncomeSource(income)
                        }
                    }
                )
            }
            
            // Fixed Expenses
            item {
                FixedExpensesCard(
                    fixedExpenses = fixedExpenses,
                    onEditExpense = { expense ->
                        scope.launch {
                            repository.saveFixedExpense(expense)
                        }
                    }
                )
            }
            
            // Variable & Discretionary Spending
            item {
                VariableSpendingCard(
                    variableExpenses = variableExpenses,
                    onEditCategory = { category ->
                        scope.launch {
                            repository.saveVariableExpenseCategory(category)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun IncomeBreakdownCard(
    incomeSources: List<IncomeSource>,
    onEditIncome: (IncomeSource) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📊 Income Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Table Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(2f)
                )
                Text(
                    text = "Gross",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Net",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Real Income Rows
            incomeSources.forEach { income ->
                EditableIncomeRow(
                    income = income,
                    onEditIncome = onEditIncome
                )
            }
            
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Total Row
            val totalMonthlyNet = incomeSources.sumOf { it.getMonthlyNetAmount() }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Monthly Net Income",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(2f)
                )
                Text(
                    text = "",
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$${String.format("%.2f", totalMonthlyNet)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF28a745),
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun EditableIncomeRow(
    income: IncomeSource,
    onEditIncome: (IncomeSource) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedGross by remember { mutableStateOf(income.grossAmount.toString()) }
    var editedTaxes by remember { mutableStateOf(income.taxesDeductions.toString()) }
    
    if (isEditing) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "Edit: ${income.description}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editedGross,
                        onValueChange = { editedGross = it },
                        label = { Text("Gross") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = editedTaxes,
                        onValueChange = { editedTaxes = it },
                        label = { Text("Taxes") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val updatedIncome = income.copy(
                                grossAmount = editedGross.toDoubleOrNull() ?: income.grossAmount,
                                taxesDeductions = editedTaxes.toDoubleOrNull() ?: income.taxesDeductions,
                                netAmount = (editedGross.toDoubleOrNull() ?: income.grossAmount) - (editedTaxes.toDoubleOrNull() ?: income.taxesDeductions),
                                updatedAt = Date()
                            )
                            onEditIncome(updatedIncome)
                            isEditing = false
                        }
                    ) {
                        Text("Save")
                    }
                    
                    OutlinedButton(
                        onClick = {
                            isEditing = false
                            editedGross = income.grossAmount.toString()
                            editedTaxes = income.taxesDeductions.toString()
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(2f)
            ) {
                Text(
                    text = income.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = income.frequency,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "$${String.format("%.2f", income.grossAmount)}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
            
            Text(
                text = "$${String.format("%.2f", income.netAmount)}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(
                onClick = { isEditing = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun FixedExpensesCard(
    fixedExpenses: List<FixedExpense>,
    onEditExpense: (FixedExpense) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "💸 Fixed Monthly Expenses",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Real Fixed Expenses - Editable
            fixedExpenses.forEach { expense ->
                EditableFixedExpenseRow(
                    expense = expense,
                    onEditExpense = onEditExpense
                )
                if (expense != fixedExpenses.last()) {
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
            
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Total Row - Calculated from real data
            val totalFixedExpenses = fixedExpenses.sumOf { it.amount }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Fixed Expenses",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "$${String.format("%.2f", totalFixedExpenses)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFdc3545)
                )
            }
        }
    }
}

@Composable
private fun EditableFixedExpenseRow(
    expense: FixedExpense,
    onEditExpense: (FixedExpense) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedAmount by remember { mutableStateOf(expense.amount.toString()) }
    var editedNotes by remember { mutableStateOf(expense.notes) }
    
    if (isEditing) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "Edit: ${expense.description}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editedAmount,
                        onValueChange = { editedAmount = it },
                        label = { Text("Amount") },
                        leadingIcon = { Text("$") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = editedNotes,
                        onValueChange = { editedNotes = it },
                        label = { Text("Notes") },
                        modifier = Modifier.weight(2f),
                        singleLine = true
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val updatedExpense = expense.copy(
                                amount = editedAmount.toDoubleOrNull() ?: expense.amount,
                                notes = editedNotes,
                                updatedAt = Date()
                            )
                            onEditExpense(updatedExpense)
                            isEditing = false
                        }
                    ) {
                        Text("Save")
                    }
                    
                    OutlinedButton(
                        onClick = {
                            isEditing = false
                            editedAmount = expense.amount.toString()
                            editedNotes = expense.notes
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${expense.category} - ${expense.description}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "Due: ${expense.dueDate} • ${expense.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${String.format("%.2f", expense.amount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFdc3545)
                    )
                    
                    IconButton(
                        onClick = { isEditing = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VariableSpendingCard(
    variableExpenses: List<VariableExpenseCategory>,
    onEditCategory: (VariableExpenseCategory) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🎯 Variable & Discretionary Spending",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            variableExpenses.forEach { expense ->
                EditableVariableExpenseRow(
                    expense = expense,
                    onEditCategory = onEditCategory
                )
                if (expense != variableExpenses.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Total Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Discretionary",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                Text(
                    text = "$${String.format("%.2f", variableExpenses.sumOf { it.budgetAmount })}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF28a745)
                )
                    
                    Text(
                        text = "Available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EditableVariableExpenseRow(
    expense: VariableExpenseCategory,
    onEditCategory: (VariableExpenseCategory) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedBudget by remember { mutableStateOf(expense.budgetAmount.toString()) }
    var editedSpent by remember { mutableStateOf(expense.spentAmount.toString()) }
    
    if (isEditing) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "Edit: ${expense.category}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editedBudget,
                        onValueChange = { editedBudget = it },
                        label = { Text("Budget") },
                        leadingIcon = { Text("$") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = editedSpent,
                        onValueChange = { editedSpent = it },
                        label = { Text("Spent") },
                        leadingIcon = { Text("$") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val updatedCategory = expense.copy(
                                budgetAmount = editedBudget.toDoubleOrNull() ?: expense.budgetAmount,
                                spentAmount = editedSpent.toDoubleOrNull() ?: expense.spentAmount,
                                updatedAt = Date()
                            )
                            onEditCategory(updatedCategory)
                            isEditing = false
                        }
                    ) {
                        Text("Save")
                    }
                    
                    OutlinedButton(
                        onClick = {
                            isEditing = false
                            editedBudget = expense.budgetAmount.toString()
                            editedSpent = expense.spentAmount.toString()
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(2f)
                ) {
                    Text(
                        text = expense.category,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = expense.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Budget: $${String.format("%.2f", expense.budgetAmount)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    Text(
                        text = "Spent: $${String.format("%.2f", expense.spentAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (expense.spentAmount > expense.budgetAmount) 
                            Color(0xFFdc3545) else Color(0xFF28a745)
                    )
                    
                    Text(
                        text = "Remaining: $${String.format("%.2f", expense.getRemainingBudget())}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                IconButton(
                    onClick = { isEditing = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// Data classes
private data class ExpenseItem(
    val category: String,
    val description: String,
    val dueDate: String,
    val amount: String,
    val notes: String
)

private data class VariableExpenseItem(
    val category: String,
    val budget: String,
    val notes: String
)
