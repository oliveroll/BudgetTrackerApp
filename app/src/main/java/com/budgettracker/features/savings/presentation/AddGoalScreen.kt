package com.budgettracker.features.savings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.budgettracker.core.data.repository.FirebaseRepository
import com.budgettracker.core.domain.model.GoalCategory
import com.budgettracker.core.domain.model.Priority
import com.budgettracker.core.domain.model.SavingsGoal
import kotlinx.coroutines.launch
import java.util.*

/**
 * Add Goal Screen for creating new savings goals
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalScreen(
    onNavigateBack: () -> Unit = {},
    onSaveGoal: (String, String, String, String, String, String) -> Unit = { _, _, _, _, _, _ -> }
) {
    val context = LocalContext.current
    val repository = remember { FirebaseRepository(context) }
    val scope = rememberCoroutineScope()
    var goalName by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var monthlyContribution by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(GoalCategory.EMERGENCY_FUND) }
    var selectedPriority by remember { mutableStateOf(Priority.MEDIUM) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showPriorityDropdown by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Add Savings Goal",
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
                            if (goalName.isNotBlank() && targetAmount.isNotBlank()) {
                                scope.launch {
                                    val goal = SavingsGoal(
                                        name = goalName,
                                        targetAmount = targetAmount.toDoubleOrNull() ?: 0.0,
                                        monthlyContribution = monthlyContribution.toDoubleOrNull() ?: 0.0,
                                        description = description,
                                        category = selectedCategory,
                                        priority = selectedPriority,
                                        deadline = Date(System.currentTimeMillis() + 31536000000L), // 1 year from now
                                        createdAt = Date(),
                                        updatedAt = Date()
                                    )
                                    repository.saveSavingsGoal(goal)
                                    onNavigateBack()
                                }
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
            // Goal Name
            OutlinedTextField(
                value = goalName,
                onValueChange = { goalName = it },
                label = { Text("Goal Name") },
                leadingIcon = { Text(selectedCategory.icon) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("e.g., Emergency Fund") }
            )
            
            // Target Amount
            OutlinedTextField(
                value = targetAmount,
                onValueChange = { targetAmount = it },
                label = { Text("Target Amount") },
                leadingIcon = { Text("$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("0.00") }
            )
            
            // Monthly Contribution
            OutlinedTextField(
                value = monthlyContribution,
                onValueChange = { monthlyContribution = it },
                label = { Text("Monthly Contribution") },
                leadingIcon = { Text("$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("0.00") }
            )
            
            // Category Selection
            ExposedDropdownMenuBox(
                expanded = showCategoryDropdown,
                onExpandedChange = { showCategoryDropdown = !showCategoryDropdown }
            ) {
                OutlinedTextField(
                    value = "${selectedCategory.icon} ${selectedCategory.displayName}",
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
                    GoalCategory.values().forEach { category ->
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
                }
            }
            
            // Priority Selection
            ExposedDropdownMenuBox(
                expanded = showPriorityDropdown,
                onExpandedChange = { showPriorityDropdown = !showPriorityDropdown }
            ) {
                OutlinedTextField(
                    value = selectedPriority.displayName,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Priority") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPriorityDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = showPriorityDropdown,
                    onDismissRequest = { showPriorityDropdown = false }
                ) {
                    Priority.values().forEach { priority ->
                        DropdownMenuItem(
                            text = { Text(priority.displayName) },
                            onClick = {
                                selectedPriority = priority
                                showPriorityDropdown = false
                            }
                        )
                    }
                }
            }
            
            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4,
                placeholder = { Text("Why is this goal important to you?") }
            )
            
            // Goal Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "💡 Goal Tips",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "• Emergency Fund: 6 months of expenses for OPT visa holders\n" +
                                "• Roth IRA: $7,000 annual limit (2024)\n" +
                                "• H1B Expenses: Budget $3,000-$5,000 for application costs\n" +
                                "• Set realistic monthly contributions based on your budget",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Save Button
            Button(
                onClick = {
                    if (goalName.isNotBlank() && targetAmount.isNotBlank()) {
                        scope.launch {
                            val goal = SavingsGoal(
                                name = goalName,
                                targetAmount = targetAmount.toDoubleOrNull() ?: 0.0,
                                monthlyContribution = monthlyContribution.toDoubleOrNull() ?: 0.0,
                                description = description,
                                category = selectedCategory,
                                priority = selectedPriority,
                                deadline = Date(System.currentTimeMillis() + 31536000000L), // 1 year from now
                                createdAt = Date(),
                                updatedAt = Date()
                            )
                            repository.saveSavingsGoal(goal)
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = goalName.isNotBlank() && targetAmount.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Goal")
            }
        }
    }
}
