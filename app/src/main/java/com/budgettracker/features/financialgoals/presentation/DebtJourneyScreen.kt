package com.budgettracker.features.financialgoals.presentation

import com.budgettracker.features.financialgoals.presentation.dialogs.AddLoanDialog
import com.budgettracker.features.financialgoals.presentation.dialogs.EditLoanDialog
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.budgettracker.core.domain.model.DebtLoan
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtJourneyScreen(
    viewModel: DebtJourneyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.loans.isEmpty()) {
            EmptyDebtState(onAddClick = { viewModel.toggleAddDialog() })
        } else {
            // Summary Card
            DebtSummaryCard(loans = uiState.loans)
            
            // Loans List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.loans) { loan ->
                    DebtLoanCard(
                        loan = loan,
                        onClick = { viewModel.selectLoan(loan) },
                        onEdit = {
                            viewModel.selectLoan(loan)
                            viewModel.toggleEditDialog()
                        },
                        onDelete = {
                            viewModel.selectLoan(loan)
                            viewModel.deleteLoan(loan.id)
                        }
                    )
                }
            }
        }
        
        // Add Loan Dialog
        if (uiState.showAddDialog) {
            AddLoanDialog(
                onDismiss = { viewModel.toggleAddDialog() },
                onConfirm = { loan ->
                    viewModel.addLoan(loan)
                }
            )
        }
        
        // Edit Loan Dialog
        if (uiState.showEditDialog && uiState.selectedLoan != null) {
            EditLoanDialog(
                loan = uiState.selectedLoan!!,
                onDismiss = { 
                    viewModel.toggleEditDialog()
                    viewModel.selectLoan(null)
                },
                onConfirm = { updatedLoan ->
                    viewModel.updateLoan(updatedLoan)
                },
                onDelete = {
                    viewModel.deleteLoan(uiState.selectedLoan!!.id)
                    viewModel.toggleEditDialog()
                }
            )
        }
        
        // Error Snackbar
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                // Show error
                viewModel.clearError()
            }
        }
        
        // Loan Detail Bottom Sheet
        if (uiState.selectedLoan != null) {
            LoanDetailSheet(
                loan = uiState.selectedLoan!!,
                onDismiss = { viewModel.selectLoan(null) },
                onRecordPayment = { viewModel.togglePaymentDialog() },
                onEdit = { viewModel.toggleEditDialog() },
                onDelete = { viewModel.deleteLoan(it) }
            )
        }
    }
}

@Composable
fun DebtSummaryCard(loans: List<DebtLoan>) {
    val totalDebt = loans.sumOf { it.currentBalance }
    val totalOriginal = loans.sumOf { it.originalAmount }
    val totalPaid = totalOriginal - totalDebt
    val progressPercentage = if (totalOriginal > 0) (totalPaid / totalOriginal * 100).toFloat() else 0f
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "Total Debt Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Remaining",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        NumberFormat.getCurrencyInstance().format(totalDebt),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFdc3545)
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Paid Off",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        NumberFormat.getCurrencyInstance().format(totalPaid),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF28a745)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${progressPercentage.toInt()}% Paid",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${loans.size} ${if (loans.size == 1) "Loan" else "Loans"}",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = progressPercentage / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF28a745),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
fun DebtLoanCard(
    loan: DebtLoan,
    onClick: () -> Unit,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        loan.loanProvider,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        loan.loanType,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        NumberFormat.getCurrencyInstance().format(loan.currentBalance),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFdc3545)
                    )
                    Text(
                        "${loan.interestRate}% APR",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            // Edit and Delete buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.padding(end = 8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Monthly Payment",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        NumberFormat.getCurrencyInstance().format(loan.getEffectiveMonthlyPayment()),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Est. Payoff",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        "${loan.getEstimatedMonthsRemaining()} months",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = loan.getProgressPercentage() / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF28a745),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                "${loan.getProgressPercentage().toInt()}% paid off",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDetailSheet(
    loan: DebtLoan,
    onDismiss: () -> Unit,
    onRecordPayment: () -> Unit,
    onEdit: () -> Unit,
    onDelete: (String) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                loan.loanProvider,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                loan.loanType,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Key Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricColumn("Current Balance", NumberFormat.getCurrencyInstance().format(loan.currentBalance))
                MetricColumn("Original Amount", NumberFormat.getCurrencyInstance().format(loan.originalAmount))
                MetricColumn("Interest Rate", "${loan.interestRate}%")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Payment Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Payment Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    InfoRow("Monthly Payment", NumberFormat.getCurrencyInstance().format(loan.getEffectiveMonthlyPayment()))
                    InfoRow("Next Due Date", dateFormat.format(loan.nextPaymentDueDate))
                    InfoRow("Estimated Payoff", "${loan.getEstimatedMonthsRemaining()} months")
                    InfoRow("Projected Date", dateFormat.format(loan.getProjectedPayoffDate()))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRecordPayment,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF28a745)
                    )
                ) {
                    Icon(Icons.Default.Payments, "Record Payment", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pay")
                }
                
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedButton(
                onClick = { onDelete(loan.id) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFdc3545)
                )
            ) {
                Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Loan")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MetricColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun EmptyDebtState(onAddClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Celebration,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No Debt Loans",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Start tracking your debt payoff journey",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onAddClick) {
                Icon(Icons.Default.Add, "Add")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Your First Loan")
            }
        }
    }
}

