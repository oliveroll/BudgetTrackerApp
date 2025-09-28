package com.budgettracker.features.reports.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Reports screen for analytics and insights
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "📊 Reports & Analytics",
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
            // Monthly Summary
            item {
                MonthlySummaryCard()
            }
            
            // Category Breakdown
            item {
                CategoryBreakdownCard()
            }
            
            // Savings Progress
            item {
                SavingsProgressCard()
            }
            
            // OPT Visa Tracking
            item {
                OPTVisaTrackingCard()
            }
        }
    }
}

@Composable
private fun MonthlySummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Monthly Summary - September 2025",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    label = "Income",
                    amount = "$5,470.00",
                    color = Color(0xFF28a745)
                )
                
                SummaryItem(
                    label = "Expenses",
                    amount = "$1,705.99",
                    color = Color(0xFFdc3545)
                )
                
                SummaryItem(
                    label = "Savings",
                    amount = "$2,000.00",
                    color = Color(0xFF007bff)
                )
                
                SummaryItem(
                    label = "Remaining",
                    amount = "$1,764.01",
                    color = Color(0xFF28a745)
                )
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
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = amount,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun CategoryBreakdownCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Category Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Top spending categories this month:",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            CategoryItem("🏠 Rent", "$700.00", 41.0f)
            CategoryItem("💳 German Loan", "$475.00", 28.0f)
            CategoryItem("🛒 Groceries", "$300.00", 18.0f)
            CategoryItem("⚡ Utilities", "$100.00", 6.0f)
            CategoryItem("📱 Other", "$130.99", 7.0f)
        }
    }
}

@Composable
private fun CategoryItem(
    category: String,
    amount: String,
    percentage: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "${percentage.toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SavingsProgressCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "💰 Savings & Investment Allocation",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SavingsAllocationItem("Emergency Fund", "High-Yield Savings", "$800.00", "$0", "$16,000")
            SavingsAllocationItem("Retirement", "Roth IRA", "$583.00", "$0", "$7,000/year")
            SavingsAllocationItem("Investments", "Robinhood ETFs", "$400.00", "$0", "Flexible")
            SavingsAllocationItem("Fun Money", "Robinhood Stocks", "$100.00", "$0", "Flexible")
            SavingsAllocationItem("Travel Fund", "Savings", "$117.00", "$0", "$1,400/year")
            
            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Savings & Investments",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "$2,000.00",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF28a745)
                )
            }
        }
    }
}

@Composable
private fun SavingsAllocationItem(
    category: String,
    accountType: String,
    monthlyTarget: String,
    ytdProgress: String,
    goal: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(2f)
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = accountType,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = monthlyTarget,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF007bff)
                )
                
                Text(
                    text = "$ytdProgress / $goal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OPTVisaTrackingCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🛂 OPT Visa Tracking",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "• Emergency Fund Target: 6 months ($32,820)\n• H1B Application Budget: $3,000\n• Work Authorization Status: Active\n• Next Review: Check quarterly",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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


