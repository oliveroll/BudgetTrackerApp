package com.budgettracker.features.budget.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Budget Overview Screen - No Hilt Dependencies
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetOverviewScreen(
    onNavigateToSubscriptions: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {},
    onNavigateToDebtTracker: () -> Unit = {}
) {
    // Static data for now - no ViewModel needed
    val isLoading = false
    val error: String? = null
    
    // Error state
    if (error != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Error: $error",
                    color = MaterialTheme.colorScheme.error
                )
                Button(
                    onClick = { /* Refresh logic */ },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Retry")
                }
            }
        }
        return
    }
    
    // Loading state
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Budget Overview",
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
            // Simple header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Financial Overview",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Net Monthly Income: $5,191.32",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
            
            // Simple debt progress
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Debt Progress",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Remaining: €10,317.64",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { 0.107f }, // 10.7%
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "10.7% paid off • Freedom Date: November 2026",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Simple subscriptions list
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Active Subscriptions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Sample subscriptions
                        listOf(
                            "Spotify Premium" to "$11.99",
                            "Phone Plan" to "$150.00",
                            "Netflix" to "$15.49"
                        ).forEach { (name, cost) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = name)
                                Text(text = cost, fontWeight = FontWeight.Medium)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Total: $177.48/month",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Simple upcoming bills
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Upcoming Bills",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Sample bills
                        listOf(
                            "Rent Payment" to "$708.95",
                            "Electric Bill" to "$85.00", 
                            "German Student Loan" to "€900.00"
                        ).forEach { (name, amount) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = name)
                                Text(text = amount, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
            
            // Action buttons
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onNavigateToSubscriptions,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Manage Subscriptions")
                    }
                    
                    OutlinedButton(
                        onClick = onNavigateToReminders,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Set Reminders")
                    }
                    
                    OutlinedButton(
                        onClick = onNavigateToDebtTracker,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View Debt Details")
                    }
                }
            }
        }
    }
}