package com.budgettracker.features.financialgoals.presentation

import com.budgettracker.features.financialgoals.presentation.dialogs.AddEmergencyFundDialog
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
import com.budgettracker.core.domain.model.EmergencyFund
import com.budgettracker.core.domain.model.EmergencyFundProjection
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyFundScreen(
    viewModel: EmergencyFundViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        floatingActionButton = {
            if (uiState.selectedFund != null) {
                FloatingActionButton(
                    onClick = { viewModel.toggleDepositDialog() },
                    containerColor = Color(0xFF28a745)
                ) {
                    Icon(Icons.Default.Add, "Add Deposit")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.selectedFund == null) {
                EmptyFundState(onAddClick = { viewModel.toggleAddDialog() })
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        FundProgressCard(fund = uiState.selectedFund!!)
                    }
                    
                    item {
                        ProjectionCard(
                            fund = uiState.selectedFund!!,
                            projections = uiState.projections,
                            onMonthsChange = { viewModel.updateProjectionMonths(it) }
                        )
                    }
                    
                    item {
                        FundDetailsCard(fund = uiState.selectedFund!!)
                    }
                    
                    item {
                        Text(
                            "12-Month Projection",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    items(uiState.projections.take(12)) { projection ->
                        ProjectionRow(projection = projection)
                    }
                }
            }
        }
        
        // Add Fund Dialog
        if (uiState.showAddDialog) {
            AddEmergencyFundDialog(
                onDismiss = { viewModel.toggleAddDialog() },
                onConfirm = { fund ->
                    viewModel.addFund(fund)
                }
            )
        }
    }
}

@Composable
fun FundProgressCard(fund: EmergencyFund) {
    val progressPercentage = fund.getProgressPercentage()
    val monthsToGoal = fund.getMonthsToReachGoal()
    val isGoalReached = fund.isGoalReached()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGoalReached) Color(0xFF28a745).copy(alpha = 0.1f)
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
                        fund.bankName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        fund.accountType,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                if (isGoalReached) {
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
                                "Goal Reached",
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "GOAL REACHED",
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
                        "Current Balance",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        NumberFormat.getCurrencyInstance().format(fund.currentBalance),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF28a745)
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        if (isGoalReached) "Above Goal" else "Remaining",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        NumberFormat.getCurrencyInstance().format(
                            if (isGoalReached) fund.currentBalance - fund.targetGoal 
                            else fund.getRemainingToGoal()
                        ),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isGoalReached) Color(0xFF28a745) else Color(0xFFfd7e14)
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
                        "${progressPercentage.toInt()}% of Goal",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Target: ${NumberFormat.getCurrencyInstance().format(fund.targetGoal)}",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = (progressPercentage / 100f).coerceAtMost(1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = if (isGoalReached) Color(0xFF28a745) else Color(0xFFdc3545),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            
            if (!isGoalReached) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Est. $monthsToGoal months to goal",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        "${fund.apy}% APY",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectionCard(
    fund: EmergencyFund,
    projections: List<EmergencyFundProjection>,
    onMonthsChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Growth Projection",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    "With ${fund.compoundingFrequency.displayName} compounding",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (projections.isNotEmpty()) {
                val finalProjection = projections.last()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProjectionMetric(
                        label = "In ${projections.size} months",
                        value = NumberFormat.getCurrencyInstance().format(finalProjection.balance),
                        icon = Icons.Default.TrendingUp
                    )
                    
                    ProjectionMetric(
                        label = "Interest Earned",
                        value = NumberFormat.getCurrencyInstance().format(
                            finalProjection.balance - fund.currentBalance - (fund.monthlyContribution * projections.size)
                        ),
                        icon = Icons.Default.AttachMoney
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectionMetric(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF28a745)
        )
    }
}

@Composable
fun FundDetailsCard(fund: EmergencyFund) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Account Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            InfoRow("Bank", fund.bankName)
            InfoRow("Account Type", fund.accountType)
            InfoRow("APY", "${fund.apy}%")
            InfoRow("Compounding", fund.compoundingFrequency.displayName)
            InfoRow("Monthly Contribution", NumberFormat.getCurrencyInstance().format(fund.monthlyContribution))
            InfoRow("Target Goal", NumberFormat.getCurrencyInstance().format(fund.targetGoal))
        }
    }
}

@Composable
fun ProjectionRow(projection: EmergencyFundProjection) {
    val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    dateFormat.format(projection.date),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Month ${projection.month}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    NumberFormat.getCurrencyInstance().format(projection.balance),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF28a745)
                )
                Text(
                    "+${NumberFormat.getCurrencyInstance().format(projection.interestEarned)} interest",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun EmptyFundState(onAddClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Shield,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No Emergency Fund Yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Build your financial safety net",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onAddClick) {
                Icon(Icons.Default.Add, "Add")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Emergency Fund")
            }
        }
    }
}

