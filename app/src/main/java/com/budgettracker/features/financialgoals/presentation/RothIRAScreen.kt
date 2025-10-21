package com.budgettracker.features.financialgoals.presentation

import com.budgettracker.features.financialgoals.presentation.dialogs.AddRothIRADialog
import com.budgettracker.features.financialgoals.presentation.dialogs.AddContributionDialog
import com.budgettracker.features.financialgoals.presentation.dialogs.EditIRADialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.budgettracker.core.domain.model.RothIRA
import java.text.NumberFormat
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RothIRAScreen(
    viewModel: RothIRAViewModel = hiltViewModel()
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
        } else if (uiState.currentYearIRA == null) {
            EmptyIRAState(onAddClick = { viewModel.toggleAddDialog() })
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    IRAProgressCard(
                        ira = uiState.currentYearIRA!!,
                        calculation = uiState.calculation
                    )
                }
                
                item {
                    ContributionCalculatorCard(
                        ira = uiState.currentYearIRA!!,
                        calculation = uiState.calculation
                    )
                }
                
                item {
                    IRADetailsCard(ira = uiState.currentYearIRA!!)
                }
                
                item {
                    QuickActionsCard(
                        onAddContribution = { viewModel.toggleContributionDialog() },
                        onEditIRA = { viewModel.toggleEditDialog() }
                    )
                }
            }
        }
    }
    
    // Add IRA Dialog
    if (uiState.showAddDialog) {
        AddRothIRADialog(
            onDismiss = { viewModel.toggleAddDialog() },
            onConfirm = { ira ->
                viewModel.addIRA(ira)
            }
        )
    }
    
    // Add Contribution Dialog
    if (uiState.showContributionDialog && uiState.currentYearIRA != null) {
        AddContributionDialog(
            ira = uiState.currentYearIRA!!,
            onDismiss = { viewModel.toggleContributionDialog() },
            onConfirm = { iraId, amount, taxYear ->
                viewModel.recordContribution(iraId, amount, taxYear)
            }
        )
    }
    
    // Edit IRA Dialog
    if (uiState.showEditDialog && uiState.currentYearIRA != null) {
        EditIRADialog(
            ira = uiState.currentYearIRA!!,
            onDismiss = { viewModel.toggleEditDialog() },
            onConfirm = { updatedIRA ->
                viewModel.updateIRA(updatedIRA)
            }
        )
    }
}

@Composable
fun IRAProgressCard(
    ira: RothIRA,
    calculation: com.budgettracker.core.domain.model.IRACalculation?
) {
    val progressPercentage = ira.getProgressPercentage()
    val isOnTrack = ira.isOnTrackToMaxOut()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (ira.isLimitReached()) Color(0xFF28a745).copy(alpha = 0.1f) 
                           else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Tax Year ${ira.taxYear}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        ira.brokerageName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                if (ira.isLimitReached()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF28a745)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                "Maxed Out",
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "MAXED OUT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Contributed",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        NumberFormat.getCurrencyInstance().format(ira.contributionsThisYear),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF28a745)
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Remaining",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        NumberFormat.getCurrencyInstance().format(ira.getRemainingContributionRoom()),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (ira.isLimitReached()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                               else Color(0xFFfd7e14)
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
                        "${progressPercentage.toInt()}% of Annual Limit",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        NumberFormat.getCurrencyInstance().format(ira.annualContributionLimit),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = progressPercentage / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = if (ira.isLimitReached()) Color(0xFF28a745) else Color(0xFF6f42c1),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            
            if (!ira.isLimitReached() && calculation != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isOnTrack) Icons.Default.TrendingUp else Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isOnTrack) Color(0xFF28a745) else Color(0xFFfd7e14)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (isOnTrack) "On Track to Max Out" else "Behind Target",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isOnTrack) Color(0xFF28a745) else Color(0xFFfd7e14),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        "${calculation.daysRemaining} days left",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun ContributionCalculatorCard(
    ira: RothIRA,
    calculation: com.budgettracker.core.domain.model.IRACalculation?
) {
    if (calculation == null || ira.isLimitReached()) return
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Calculate,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Contribution Calculator",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "To max out by year-end, contribute:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CalculatorOption(
                    frequency = "Biweekly",
                    amount = calculation.recommendedBiweeklyAmount,
                    periods = calculation.contributionsRemaining,
                    icon = Icons.Default.CalendarMonth
                )
                
                CalculatorOption(
                    frequency = "Monthly",
                    amount = calculation.recommendedMonthlyAmount,
                    periods = (calculation.daysRemaining / 30).toInt(),
                    icon = Icons.Default.Event
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Current projection: ${NumberFormat.getCurrencyInstance().format(calculation.projectedYearEnd)} by year-end",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorOption(
    frequency: String,
    amount: Double,
    periods: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            frequency,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            NumberFormat.getCurrencyInstance().format(amount),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6f42c1)
        )
        Text(
            "×$periods payments",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun IRADetailsCard(ira: RothIRA) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Account Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            InfoRow("Brokerage", ira.brokerageName)
            InfoRow("Account", "****${ira.accountNumber.takeLast(4)}")
            InfoRow("Current Balance", NumberFormat.getCurrencyInstance().format(ira.currentBalance))
            InfoRow("Annual Limit", NumberFormat.getCurrencyInstance().format(ira.annualContributionLimit))
            
            if (ira.recurringContributionAmount != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Recurring Contribution",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(
                    "Amount",
                    "${NumberFormat.getCurrencyInstance().format(ira.recurringContributionAmount)} ${ira.recurringContributionFrequency.displayName}"
                )
            }
        }
    }
}

@Composable
fun QuickActionsCard(
    onAddContribution: () -> Unit,
    onEditIRA: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddContribution,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF28a745)
                    )
                ) {
                    Icon(Icons.Default.AddCard, "Contribute", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Contribute")
                }
                
                OutlinedButton(
                    onClick = onEditIRA,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit IRA")
                }
            }
        }
    }
}

@Composable
fun EmptyIRAState(onAddClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Savings,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No Roth IRA Yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Start saving for retirement with a Roth IRA",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onAddClick) {
                Icon(Icons.Default.Add, "Add")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Setup Roth IRA for ${Calendar.getInstance().get(Calendar.YEAR)}")
            }
        }
    }
}

