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
 * Simplified Subscriptions Management Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsScreen(
    onNavigateBack: () -> Unit = {}
) {
    // Sample subscription data that can be modified
    var subscriptions by remember {
        mutableStateOf(
            listOf(
                SubscriptionItem("1", "Spotify Premium", 11.99, "Monthly", "Entertainment", true),
                SubscriptionItem("2", "Phone Plan", 150.0, "Semi-Annual", "Utilities", true),
                SubscriptionItem("3", "Netflix", 15.49, "Monthly", "Entertainment", false),
                SubscriptionItem("4", "Adobe Creative", 52.99, "Monthly", "Software", true),
                SubscriptionItem("5", "Gym Membership", 29.99, "Monthly", "Health", true)
            )
        )
    }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var editingSubscription by remember { mutableStateOf<SubscriptionItem?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Subscriptions", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Subscription")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Subscription")
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
                        text = "Subscription Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Active: ${subscriptions.count { it.isActive }}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Total Monthly: $${String.format("%.2f", subscriptions.filter { it.isActive }.sumOf { it.monthlyCost })}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Subscriptions List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(subscriptions) { subscription ->
                    SubscriptionCard(
                        subscription = subscription,
                        onEdit = { editingSubscription = it },
                        onToggleActive = { id ->
                            subscriptions = subscriptions.map { 
                                if (it.id == id) it.copy(isActive = !it.isActive) else it 
                            }
                        },
                        onDelete = { id ->
                            subscriptions = subscriptions.filter { it.id != id }
                        }
                    )
                }
            }
        }
    }
    
    // Add/Edit Dialog
    if (showAddDialog || editingSubscription != null) {
        SubscriptionDialog(
            subscription = editingSubscription,
            onDismiss = { 
                showAddDialog = false
                editingSubscription = null
            },
            onSave = { subscription ->
                if (editingSubscription != null) {
                    // Edit existing
                    subscriptions = subscriptions.map { 
                        if (it.id == subscription.id) subscription else it 
                    }
                } else {
                    // Add new
                    subscriptions = subscriptions + subscription.copy(id = UUID.randomUUID().toString())
                }
                showAddDialog = false
                editingSubscription = null
            }
        )
    }
}

@Composable
private fun SubscriptionCard(
    subscription: SubscriptionItem,
    onEdit: (SubscriptionItem) -> Unit,
    onToggleActive: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (subscription.isActive) 
                MaterialTheme.colorScheme.surface else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
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
                        text = subscription.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subscription.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format("%.2f", subscription.cost)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = subscription.frequency,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                        checked = subscription.isActive,
                        onCheckedChange = { onToggleActive(subscription.id) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (subscription.isActive) "Active" else "Inactive",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Row {
                    IconButton(onClick = { onEdit(subscription) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { onDelete(subscription.id) }) {
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
private fun SubscriptionDialog(
    subscription: SubscriptionItem?,
    onDismiss: () -> Unit,
    onSave: (SubscriptionItem) -> Unit
) {
    var name by remember { mutableStateOf(subscription?.name ?: "") }
    var cost by remember { mutableStateOf(subscription?.cost?.toString() ?: "") }
    var frequency by remember { mutableStateOf(subscription?.frequency ?: "Monthly") }
    var category by remember { mutableStateOf(subscription?.category ?: "Entertainment") }
    var isActive by remember { mutableStateOf(subscription?.isActive ?: true) }
    
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
                    text = if (subscription != null) "Edit Subscription" else "Add Subscription",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Service Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = cost,
                    onValueChange = { cost = it },
                    label = { Text("Cost") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = frequency,
                    onValueChange = { frequency = it },
                    label = { Text("Frequency (Monthly, Yearly, etc.)") },
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
                            if (name.isNotBlank() && cost.isNotBlank()) {
                                val newSubscription = SubscriptionItem(
                                    id = subscription?.id ?: "",
                                    name = name,
                                    cost = cost.toDoubleOrNull() ?: 0.0,
                                    frequency = frequency,
                                    category = category,
                                    isActive = isActive
                                )
                                onSave(newSubscription)
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

// Data class for subscription items
data class SubscriptionItem(
    val id: String,
    val name: String,
    val cost: Double,
    val frequency: String,
    val category: String,
    val isActive: Boolean
) {
    val monthlyCost: Double
        get() = when (frequency.lowercase()) {
            "weekly" -> cost * 4.33
            "monthly" -> cost
            "quarterly" -> cost / 3
            "semi-annual" -> cost / 6
            "yearly" -> cost / 12
            else -> cost
        }
}