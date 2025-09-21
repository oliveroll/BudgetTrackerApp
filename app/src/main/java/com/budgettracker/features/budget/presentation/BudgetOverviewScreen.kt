package com.budgettracker.features.budget.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Budget overview screen showing current month budget status
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetOverviewScreen(
    onNavigateToBudgetSetup: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Budget",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToBudgetSetup
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Budget"
                )
            }
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
            // Current Month Budget Card
            item {
                CurrentMonthBudgetCard()
            }
            
            // Budget Templates
            item {
                BudgetTemplatesCard(onCreateBudget = onNavigateToBudgetSetup)
            }
            
            // Empty State
            item {
                EmptyBudgetCard(onCreateBudget = onNavigateToBudgetSetup)
            }
        }
    }
}

@Composable
private fun CurrentMonthBudgetCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "September 2025",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No budget set for this month",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BudgetTemplatesCard(
    onCreateBudget: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Budget Templates",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            BudgetTemplateItem(
                title = "50/30/20 Rule",
                description = "50% needs, 30% wants, 20% savings",
                onClick = onCreateBudget
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            BudgetTemplateItem(
                title = "OPT Student Budget",
                description = "Optimized for visa holders with higher emergency fund",
                onClick = onCreateBudget
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            BudgetTemplateItem(
                title = "Zero-Based Budget",
                description = "Every dollar has a purpose",
                onClick = onCreateBudget
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetTemplateItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyBudgetCard(
    onCreateBudget: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📊",
                style = MaterialTheme.typography.displayMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No Budget Set",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Create your first budget to start tracking your spending and reach your financial goals",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onCreateBudget
            ) {
                Text("Create Budget")
            }
        }
    }
}

