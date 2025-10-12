package com.budgettracker.features.budget.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.text.KeyboardOptions
import java.text.SimpleDateFormat
import java.util.*

/**
 * Simplified Reminders Management Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    onNavigateBack: () -> Unit = {}
) {
    // Sample reminder data that can be modified
    var reminders by remember {
        mutableStateOf(
            listOf(
                ReminderItem("1", "Rent Payment", 708.95, getDateInDays(31), "Housing", "Upcoming", true),
                ReminderItem("2", "Electric Bill", 85.0, getDateInDays(5), "Utilities", "Upcoming", true),
                ReminderItem("3", "German Student Loan", 900.0, getDateInDays(58), "Debt", "Upcoming", true),
                ReminderItem("4", "Internet Bill", 79.99, getDateInDays(15), "Utilities", "Upcoming", true),
                ReminderItem("5", "Car Insurance", 145.0, getDateInDays(22), "Insurance", "Upcoming", false)
            )
        )
    }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<ReminderItem?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Reminders", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Reminder")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Reminder")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Reminders Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Active: ${reminders.count { it.isEnabled }}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Total Amount: $${String.format("%.2f", reminders.filter { it.isEnabled }.sumOf { it.amount })}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Next Due: ${getNextDueReminder(reminders)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Reminders List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reminders.sortedBy { it.dueDate }) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        onEdit = { editingReminder = it },
                        onToggleEnabled = { id ->
                            reminders = reminders.map { 
                                if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it 
                            }
                        },
                        onMarkAsPaid = { id ->
                            reminders = reminders.map { 
                                if (it.id == id) it.copy(status = "Paid") else it 
                            }
                        },
                        onDelete = { id ->
                            reminders = reminders.filter { it.id != id }
                        }
                    )
                }
            }
        }
    }
    
    // Add/Edit Dialog
    if (showAddDialog || editingReminder != null) {
        ReminderDialog(
            reminder = editingReminder,
            onDismiss = { 
                showAddDialog = false
                editingReminder = null
            },
            onSave = { reminder ->
                if (editingReminder != null) {
                    // Edit existing
                    reminders = reminders.map { 
                        if (it.id == reminder.id) reminder else it 
                    }
                } else {
                    // Add new
                    reminders = reminders + reminder.copy(id = UUID.randomUUID().toString())
                }
                showAddDialog = false
                editingReminder = null
            }
        )
    }
}

@Composable
private fun ReminderCard(
    reminder: ReminderItem,
    onEdit: (ReminderItem) -> Unit,
    onToggleEnabled: (String) -> Unit,
    onMarkAsPaid: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val daysUntilDue = ((reminder.dueDate.time - Date().time) / (1000 * 60 * 60 * 24)).toInt()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                reminder.status == "Paid" -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                daysUntilDue < 0 -> MaterialTheme.colorScheme.errorContainer
                daysUntilDue <= 3 -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = reminder.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Due: ${dateFormat.format(reminder.dueDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            daysUntilDue < 0 -> MaterialTheme.colorScheme.error
                            daysUntilDue <= 3 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format("%.2f", reminder.amount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            reminder.status == "Paid" -> MaterialTheme.colorScheme.surfaceVariant
                            daysUntilDue < 0 -> MaterialTheme.colorScheme.errorContainer
                            daysUntilDue <= 3 -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        },
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            text = when {
                                reminder.status == "Paid" -> "Paid"
                                daysUntilDue < 0 -> "Overdue"
                                daysUntilDue == 0 -> "Due Today"
                                daysUntilDue <= 3 -> "Due Soon"
                                else -> "${daysUntilDue}d"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = reminder.isEnabled,
                        onCheckedChange = { onToggleEnabled(reminder.id) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (reminder.isEnabled) "Enabled" else "Disabled",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Row {
                    if (reminder.status != "Paid") {
                        IconButton(onClick = { onMarkAsPaid(reminder.id) }) {
                            Icon(
                                Icons.Default.CheckCircle, 
                                contentDescription = "Mark as Paid",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = { onEdit(reminder) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { onDelete(reminder.id) }) {
                        Icon(
                            Icons.Default.Delete, 
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderDialog(
    reminder: ReminderItem?,
    onDismiss: () -> Unit,
    onSave: (ReminderItem) -> Unit
) {
    var title by remember { mutableStateOf(reminder?.title ?: "") }
    var amount by remember { mutableStateOf(reminder?.amount?.toString() ?: "") }
    var category by remember { mutableStateOf(reminder?.category ?: "Utilities") }
    var daysFromNow by remember { mutableStateOf("30") }
    var isEnabled by remember { mutableStateOf(reminder?.isEnabled ?: true) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = if (reminder != null) "Edit Reminder" else "Add Reminder",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Bill Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = daysFromNow,
                    onValueChange = { daysFromNow = it },
                    label = { Text("Days from now") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (title.isNotBlank() && amount.isNotBlank()) {
                                val days = daysFromNow.toIntOrNull() ?: 30
                                val newReminder = ReminderItem(
                                    id = reminder?.id ?: "",
                                    title = title,
                                    amount = amount.toDoubleOrNull() ?: 0.0,
                                    dueDate = getDateInDays(days),
                                    category = category,
                                    status = "Upcoming",
                                    isEnabled = isEnabled
                                )
                                onSave(newReminder)
                            }
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

// Helper functions
private fun getDateInDays(days: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, days)
    return calendar.time
}

private fun getNextDueReminder(reminders: List<ReminderItem>): String {
    val nextReminder = reminders
        .filter { it.isEnabled && it.status != "Paid" }
        .minByOrNull { it.dueDate }
    
    return if (nextReminder != null) {
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        "${nextReminder.title} on ${dateFormat.format(nextReminder.dueDate)}"
    } else {
        "No upcoming reminders"
    }
}

// Data class for reminder items
data class ReminderItem(
    val id: String,
    val title: String,
    val amount: Double,
    val dueDate: Date,
    val category: String,
    val status: String, // "Upcoming", "Overdue", "Paid"
    val isEnabled: Boolean
)