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
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLoanDialog(
    onDismiss: () -> Unit,
    onConfirm: (DebtLoan) -> Unit
) {
    var loanProvider by remember { mutableStateOf("") }
    var loanType by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var originalAmount by remember { mutableStateOf("") }
    var currentBalance by remember { mutableStateOf("") }
    var interestRate by remember { mutableStateOf("") }
    var monthlyPayment by remember { mutableStateOf("") }
    
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
                        text = "Add Debt Loan",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
                
                Divider()
                
                // Form fields
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
                    placeholder = { Text("0.00") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = currentBalance,
                    onValueChange = { currentBalance = it },
                    label = { Text("Current Balance *") },
                    placeholder = { Text("0.00") },
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
                            val balance = currentBalance.toDoubleOrNull() ?: 0.0
                            val rate = interestRate.toDoubleOrNull() ?: 0.0
                            val payment = monthlyPayment.toDoubleOrNull() ?: 0.0
                            
                            if (loanProvider.isNotBlank() && balance > 0 && rate > 0 && payment > 0) {
                                val loan = DebtLoan(
                                    id = UUID.randomUUID().toString(),
                                    userId = "", // Will be set by repository
                                    loanProvider = loanProvider.trim(),
                                    loanType = loanType.trim().ifBlank { "Personal Loan" },
                                    accountNumber = accountNumber.trim(),
                                    originalAmount = originalAmount.toDoubleOrNull() ?: balance,
                                    currentBalance = balance,
                                    interestRate = rate,
                                    repaymentStartDate = Date(),
                                    currentMonthlyPayment = payment,
                                    adjustedMonthlyPayment = null,
                                    nextPaymentDueDate = Date(),
                                    isActive = true,
                                    createdAt = Date(),
                                    updatedAt = Date()
                                )
                                onConfirm(loan)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = loanProvider.isNotBlank() && 
                                 currentBalance.toDoubleOrNull() != null &&
                                 interestRate.toDoubleOrNull() != null &&
                                 monthlyPayment.toDoubleOrNull() != null
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Loan")
                    }
                }
            }
        }
    }
}

