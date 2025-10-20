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
import com.budgettracker.core.domain.model.DebtLoan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLoanDialog(
    loan: DebtLoan,
    onDismiss: () -> Unit,
    onConfirm: (DebtLoan) -> Unit,
    onDelete: () -> Unit
) {
    var loanProvider by remember { mutableStateOf(loan.loanProvider) }
    var loanType by remember { mutableStateOf(loan.loanType) }
    var accountNumber by remember { mutableStateOf(loan.accountNumber) }
    var originalAmount by remember { mutableStateOf(loan.originalAmount.toString()) }
    var currentBalance by remember { mutableStateOf(loan.currentBalance.toString()) }
    var interestRate by remember { mutableStateOf(loan.interestRate.toString()) }
    var monthlyPayment by remember { mutableStateOf(loan.currentMonthlyPayment.toString()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
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
                        text = "Edit Debt Loan",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "Close")
                        }
                    }
                }
                
                HorizontalDivider()
                
                // Form fields (same as Add but pre-filled)
                OutlinedTextField(
                    value = loanProvider,
                    onValueChange = { loanProvider = it },
                    label = { Text("Loan Provider") },
                    placeholder = { Text("e.g., KfW, Chase, Wells Fargo") },
                    leadingIcon = { Icon(Icons.Default.AccountBalance, null) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = loanType,
                    onValueChange = { loanType = it },
                    label = { Text("Loan Type") },
                    placeholder = { Text("e.g., Student Loan, Personal Loan") },
                    leadingIcon = { Icon(Icons.Default.Description, null) },
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
                    value = originalAmount,
                    onValueChange = { originalAmount = it },
                    label = { Text("Original Amount") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = currentBalance,
                    onValueChange = { currentBalance = it },
                    label = { Text("Current Balance *") },
                    leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = interestRate,
                    onValueChange = { interestRate = it },
                    label = { Text("Interest Rate (APR) *") },
                    placeholder = { Text("e.g., 6.66") },
                    leadingIcon = { Icon(Icons.Default.Percent, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = monthlyPayment,
                    onValueChange = { monthlyPayment = it },
                    label = { Text("Monthly Payment *") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                HorizontalDivider()
                
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
                            val balance = currentBalance.toDoubleOrNull() ?: loan.currentBalance
                            val rate = interestRate.toDoubleOrNull() ?: loan.interestRate
                            val payment = monthlyPayment.toDoubleOrNull() ?: loan.currentMonthlyPayment
                            
                            val updatedLoan = loan.copy(
                                loanProvider = loanProvider.trim(),
                                loanType = loanType.trim(),
                                accountNumber = accountNumber.trim(),
                                originalAmount = originalAmount.toDoubleOrNull() ?: loan.originalAmount,
                                currentBalance = balance,
                                interestRate = rate,
                                currentMonthlyPayment = payment,
                                updatedAt = java.util.Date()
                            )
                            onConfirm(updatedLoan)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = loanProvider.isNotBlank() && 
                                 currentBalance.toDoubleOrNull() != null &&
                                 interestRate.toDoubleOrNull() != null &&
                                 monthlyPayment.toDoubleOrNull() != null
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Update")
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Loan?") },
            text = { Text("Are you sure you want to delete this loan? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

