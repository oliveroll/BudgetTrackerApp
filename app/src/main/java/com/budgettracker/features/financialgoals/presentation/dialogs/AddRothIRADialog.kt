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
import com.budgettracker.core.domain.model.RothIRA
import com.budgettracker.core.domain.model.ContributionFrequency
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRothIRADialog(
    onDismiss: () -> Unit,
    onConfirm: (RothIRA) -> Unit
) {
    var brokerageName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var currentBalance by remember { mutableStateOf("") }
    var annualLimit by remember { mutableStateOf("7000") } // 2024 limit
    var contributionsThisYear by remember { mutableStateOf("") }
    var recurringAmount by remember { mutableStateOf("") }
    var selectedFrequency by remember { mutableStateOf(ContributionFrequency.BIWEEKLY) }
    var expanded by remember { mutableStateOf(false) }
    
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
                        text = "Setup Roth IRA",
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
                    placeholder = { Text("e.g., Fidelity, Vanguard, Schwab") },
                    leadingIcon = { Icon(Icons.Default.AccountBalance, null) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = { Text("Account Number (Optional)") },
                    placeholder = { Text("Last 4 digits") },
                    leadingIcon = { Icon(Icons.Default.Tag, null) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = currentBalance,
                    onValueChange = { currentBalance = it },
                    label = { Text("Current Balance") },
                    placeholder = { Text("0.00") },
                    leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = annualLimit,
                    onValueChange = { annualLimit = it },
                    label = { Text("Annual Contribution Limit") },
                    placeholder = { Text("7000") },
                    leadingIcon = { Icon(Icons.Default.TrendingUp, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    supportingText = { Text("2024 limit: $7,000 (or $8,000 if 50+)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = contributionsThisYear,
                    onValueChange = { contributionsThisYear = it },
                    label = { Text("Contributions This Year") },
                    placeholder = { Text("0.00") },
                    leadingIcon = { Icon(Icons.Default.Savings, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = "Recurring Contribution (Optional)",
                    style = MaterialTheme.typography.titleSmall
                )
                
                OutlinedTextField(
                    value = recurringAmount,
                    onValueChange = { recurringAmount = it },
                    label = { Text("Amount") },
                    placeholder = { Text("0.00") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Frequency dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedFrequency.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Frequency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ContributionFrequency.values().forEach { frequency ->
                            DropdownMenuItem(
                                text = { Text(frequency.displayName) },
                                onClick = {
                                    selectedFrequency = frequency
                                    expanded = false
                                }
                            )
                        }
                    }
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
                            val ira = RothIRA(
                                id = UUID.randomUUID().toString(),
                                userId = "",
                                brokerageName = brokerageName.trim(),
                                accountNumber = accountNumber.trim(),
                                currentBalance = currentBalance.toDoubleOrNull() ?: 0.0,
                                annualContributionLimit = annualLimit.toDoubleOrNull() ?: 7000.0,
                                contributionsThisYear = contributionsThisYear.toDoubleOrNull() ?: 0.0,
                                taxYear = Calendar.getInstance().get(Calendar.YEAR),
                                recurringContributionAmount = recurringAmount.toDoubleOrNull(),
                                recurringContributionFrequency = selectedFrequency,
                                recurringContributionStartDate = if (recurringAmount.isNotBlank()) Date() else null,
                                isActive = true,
                                createdAt = Date(),
                                updatedAt = Date()
                            )
                            onConfirm(ira)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = brokerageName.isNotBlank()
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Setup IRA")
                    }
                }
            }
        }
    }
}

