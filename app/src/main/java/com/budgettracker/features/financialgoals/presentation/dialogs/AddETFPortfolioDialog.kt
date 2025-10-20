package com.budgettracker.features.financialgoals.presentation.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.budgettracker.core.domain.model.ETFPortfolio
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddETFPortfolioDialog(
    onDismiss: () -> Unit,
    onConfirm: (ETFPortfolio) -> Unit
) {
    var brokerageName by remember { mutableStateOf("") }
    var accountType by remember { mutableStateOf("Brokerage Account") }
    var targetMonthlyInvestment by remember { mutableStateOf("") }
    var usStockAllocation by remember { mutableStateOf("70") }
    var intlStockAllocation by remember { mutableStateOf("20") }
    var bondAllocation by remember { mutableStateOf("10") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Create ETF Portfolio",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
                
                Divider()
                
                // Form fields
                OutlinedTextField(
                    value = brokerageName,
                    onValueChange = { brokerageName = it },
                    label = { Text("Brokerage Name *") },
                    placeholder = { Text("e.g., Robinhood, Fidelity, Vanguard") },
                    leadingIcon = { Icon(Icons.Default.AccountBalance, null) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = accountType,
                    onValueChange = { accountType = it },
                    label = { Text("Account Type") },
                    placeholder = { Text("e.g., Individual, Roth IRA") },
                    leadingIcon = { Icon(Icons.Default.Description, null) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = targetMonthlyInvestment,
                    onValueChange = { targetMonthlyInvestment = it },
                    label = { Text("Target Monthly Investment") },
                    placeholder = { Text("0.00") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = "Target Allocation (%)",
                    style = MaterialTheme.typography.titleSmall
                )
                
                OutlinedTextField(
                    value = usStockAllocation,
                    onValueChange = { usStockAllocation = it },
                    label = { Text("U.S. Stocks") },
                    placeholder = { Text("70") },
                    leadingIcon = { Icon(Icons.Default.TrendingUp, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    supportingText = { Text("e.g., VOO, VTI") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = intlStockAllocation,
                    onValueChange = { intlStockAllocation = it },
                    label = { Text("International Stocks") },
                    placeholder = { Text("20") },
                    leadingIcon = { Icon(Icons.Default.Public, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    supportingText = { Text("e.g., VXUS, VEA") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = bondAllocation,
                    onValueChange = { bondAllocation = it },
                    label = { Text("Bonds") },
                    placeholder = { Text("10") },
                    leadingIcon = { Icon(Icons.Default.Shield, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    supportingText = { Text("e.g., BND, AGG") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                val totalAllocation = (usStockAllocation.toDoubleOrNull() ?: 0.0) +
                                    (intlStockAllocation.toDoubleOrNull() ?: 0.0) +
                                    (bondAllocation.toDoubleOrNull() ?: 0.0)
                
                if (totalAllocation != 100.0 && totalAllocation > 0) {
                    Text(
                        text = "⚠️ Total allocation: ${totalAllocation.toInt()}% (should be 100%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                Divider()
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val usAllocation = usStockAllocation.toDoubleOrNull() ?: 70.0
                            val intlAllocation = intlStockAllocation.toDoubleOrNull() ?: 20.0
                            val bondsAllocation = bondAllocation.toDoubleOrNull() ?: 10.0
                            
                            val portfolio = ETFPortfolio(
                                id = UUID.randomUUID().toString(),
                                userId = "",
                                brokerageName = brokerageName.trim(),
                                accountType = accountType.trim(),
                                targetMonthlyInvestment = targetMonthlyInvestment.toDoubleOrNull() ?: 0.0,
                                targetAllocation = mapOf(
                                    "US_STOCKS" to usAllocation,
                                    "INTL_STOCKS" to intlAllocation,
                                    "BONDS" to bondsAllocation
                                ),
                                isActive = true,
                                createdAt = Date(),
                                updatedAt = Date()
                            )
                            onConfirm(portfolio)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = brokerageName.isNotBlank()
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Create Portfolio")
                    }
                }
            }
        }
    }
}

