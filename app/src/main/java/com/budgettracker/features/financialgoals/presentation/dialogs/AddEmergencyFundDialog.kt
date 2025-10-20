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
import com.budgettracker.core.domain.model.EmergencyFund
import com.budgettracker.core.domain.model.CompoundingFrequency
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEmergencyFundDialog(
    onDismiss: () -> Unit,
    onConfirm: (EmergencyFund) -> Unit
) {
    var bankName by remember { mutableStateOf("") }
    var accountType by remember { mutableStateOf("Savings Account") }
    var currentBalance by remember { mutableStateOf("") }
    var targetGoal by remember { mutableStateOf("") }
    var apy by remember { mutableStateOf("") }
    var monthlyContribution by remember { mutableStateOf("") }
    var selectedCompounding by remember { mutableStateOf(CompoundingFrequency.MONTHLY) }
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
                        text = "Setup Emergency Fund",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
                
                Divider()
                
                // Form fields
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Bank Name *") },
                    placeholder = { Text("e.g., Ally Bank, Marcus, Discover") },
                    leadingIcon = { Icon(Icons.Default.AccountBalance, null) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = accountType,
                    onValueChange = { accountType = it },
                    label = { Text("Account Type") },
                    placeholder = { Text("e.g., High Yield Savings") },
                    leadingIcon = { Icon(Icons.Default.Savings, null) },
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
                    value = targetGoal,
                    onValueChange = { targetGoal = it },
                    label = { Text("Target Goal *") },
                    placeholder = { Text("5000.00") },
                    leadingIcon = { Icon(Icons.Default.Flag, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    supportingText = { Text("Recommended: 3-6 months of expenses") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = apy,
                    onValueChange = { apy = it },
                    label = { Text("APY (Annual Percentage Yield)") },
                    placeholder = { Text("e.g., 4.5") },
                    leadingIcon = { Icon(Icons.Default.Percent, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Compounding frequency dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCompounding.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Compounding Frequency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        CompoundingFrequency.values().forEach { frequency ->
                            DropdownMenuItem(
                                text = { Text(frequency.displayName) },
                                onClick = {
                                    selectedCompounding = frequency
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = monthlyContribution,
                    onValueChange = { monthlyContribution = it },
                    label = { Text("Monthly Contribution") },
                    placeholder = { Text("0.00") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
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
                            val fund = EmergencyFund(
                                id = UUID.randomUUID().toString(),
                                userId = "",
                                bankName = bankName.trim(),
                                accountType = accountType.trim(),
                                currentBalance = currentBalance.toDoubleOrNull() ?: 0.0,
                                targetGoal = targetGoal.toDoubleOrNull() ?: 5000.0,
                                apy = apy.toDoubleOrNull() ?: 0.0,
                                compoundingFrequency = selectedCompounding,
                                monthlyContribution = monthlyContribution.toDoubleOrNull() ?: 0.0,
                                isActive = true,
                                createdAt = Date(),
                                updatedAt = Date()
                            )
                            onConfirm(fund)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = bankName.isNotBlank() && targetGoal.toDoubleOrNull() != null
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Create Fund")
                    }
                }
            }
        }
    }
}

