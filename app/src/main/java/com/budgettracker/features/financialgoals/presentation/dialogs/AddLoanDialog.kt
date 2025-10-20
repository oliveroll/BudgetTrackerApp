package com.budgettracker.features.financialgoals.presentation.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF667eea),
                                Color(0xFF764ba2)
                            )
                        )
                    )
                    .padding(28.dp)
            ) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    // Header with icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Add Debt Loan",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Loan Provider
                    OutlinedTextField(
                        value = loanProvider,
                        onValueChange = { loanProvider = it },
                        label = { Text("Loan Provider *", color = Color.White.copy(alpha = 0.8f)) },
                        placeholder = { Text("e.g., KfW, Chase", color = Color.White.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                            cursorColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Loan Type
                    OutlinedTextField(
                        value = loanType,
                        onValueChange = { loanType = it },
                        label = { Text("Loan Type", color = Color.White.copy(alpha = 0.8f)) },
                        placeholder = { Text("e.g., Student Loan", color = Color.White.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                            cursorColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Account Number (Optional)
                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it },
                        label = { Text("Account Number (Optional)", color = Color.White.copy(alpha = 0.8f)) },
                        placeholder = { Text("Last 4 digits", color = Color.White.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                            cursorColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Current Balance
                    OutlinedTextField(
                        value = currentBalance,
                        onValueChange = { currentBalance = it },
                        label = { Text("Current Balance *", color = Color.White.copy(alpha = 0.8f)) },
                        placeholder = { Text("0.00", color = Color.White.copy(alpha = 0.5f)) },
                        prefix = { Text("$", color = Color.White) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                            cursorColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Interest Rate
                    OutlinedTextField(
                        value = interestRate,
                        onValueChange = { interestRate = it },
                        label = { Text("Interest Rate (APR) *", color = Color.White.copy(alpha = 0.8f)) },
                        placeholder = { Text("e.g., 6.66", color = Color.White.copy(alpha = 0.5f)) },
                        suffix = { Text("%", color = Color.White) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                            cursorColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Monthly Payment
                    OutlinedTextField(
                        value = monthlyPayment,
                        onValueChange = { monthlyPayment = it },
                        label = { Text("Monthly Payment *", color = Color.White.copy(alpha = 0.8f)) },
                        placeholder = { Text("0.00", color = Color.White.copy(alpha = 0.5f)) },
                        prefix = { Text("$", color = Color.White) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                            cursorColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Medium)
                        }
                        
                        Button(
                            onClick = {
                                val balance = currentBalance.toDoubleOrNull() ?: 0.0
                                val rate = interestRate.toDoubleOrNull() ?: 0.0
                                val payment = monthlyPayment.toDoubleOrNull() ?: 0.0
                                
                                if (loanProvider.isNotBlank() && balance > 0 && rate > 0 && payment > 0) {
                                    val loan = DebtLoan(
                                        id = UUID.randomUUID().toString(),
                                        userId = "",
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
                                     monthlyPayment.toDoubleOrNull() != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF667eea),
                                disabledContainerColor = Color.White.copy(alpha = 0.3f),
                                disabledContentColor = Color.White.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Loan", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
