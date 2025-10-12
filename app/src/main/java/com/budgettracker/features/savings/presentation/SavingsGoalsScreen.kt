package com.budgettracker.features.savings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgettracker.core.data.repository.FirebaseRepository
import com.budgettracker.core.domain.model.SavingsGoal
import com.budgettracker.ui.theme.Primary40

/**
 * Savings Goals screen matching the design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsGoalsScreen(
    onNavigateToAddGoal: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { FirebaseRepository(context) }
    val savingsGoals by repository.getSavingsGoalsFlow().collectAsState(initial = emptyList())
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📈",
                            fontSize = 28.sp
                        )
                        Text(
                            text = "Goals",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddGoal,
                containerColor = Primary40
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Goal",
                    tint = Color.White
                )
            }
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
            // Priority Timeline
            item {
                PriorityTimelineCard()
            }
            
            // Current Goals
            items(savingsGoals) { goal ->
                RealGoalCard(goal = goal)
            }
            
            // Account Structure
            item {
                AccountStructureCard()
            }
        }
    }
}

@Composable
private fun PriorityTimelineCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🎯 Priority Timeline",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TimelinePhase(
                phase = "Phase 1",
                timeline = "Months 1-6",
                focus = "Build Emergency Fund to $10k + Start Roth IRA",
                target = "$13,500"
            )
            
            TimelinePhase(
                phase = "Phase 2",
                timeline = "Months 7-12",
                focus = "Complete Emergency Fund + Max Roth IRA",
                target = "$15,500"
            )
            
            TimelinePhase(
                phase = "Phase 3",
                timeline = "Year 2",
                focus = "Aggressive Loan Payoff + Build Investment Portfolio",
                target = "$24,000"
            )
        }
    }
}

@Composable
private fun TimelinePhase(
    phase: String,
    timeline: String,
    focus: String,
    target: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "$phase • $timeline",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = focus,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = target,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Primary40
            )
        }
    }
}

@Composable
private fun RealGoalCard(goal: SavingsGoal) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = goal.icon,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = goal.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = goal.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Text(
                    text = "$${String.format("%.0f", goal.currentAmount)} / $${String.format("%.0f", goal.targetAmount)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = goal.getProgressPercentage() / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(android.graphics.Color.parseColor(goal.color)),
                trackColor = Color.LightGray.copy(alpha = 0.3f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Monthly: $${String.format("%.2f", goal.monthlyContribution)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GoalCard(goal: SampleGoal) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = goal.icon,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = goal.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Text(
                    text = "${goal.current} / ${goal.target}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = goal.progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = goal.color,
                trackColor = Color.LightGray.copy(alpha = 0.3f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Monthly: ${goal.monthlyTarget}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AccountStructureCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📝 Account Structure",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AccountItem(
                name = "Bills & Thrills Checking",
                bank = "Your Bank",
                purpose = "Daily expenses & bills",
                monthlyFlow = "+$3,470"
            )
            
            AccountItem(
                name = "Emergency Savings",
                bank = "High-Yield (Ally/Marcus)",
                purpose = "3-4 months expenses",
                monthlyFlow = "+$800"
            )
            
            AccountItem(
                name = "Roth IRA",
                bank = "Vanguard/Fidelity",
                purpose = "Tax-free retirement",
                monthlyFlow = "+$583"
            )
            
            AccountItem(
                name = "Robinhood",
                bank = "Robinhood",
                purpose = "ETFs & fun investing",
                monthlyFlow = "+$500"
            )
            
            AccountItem(
                name = "Travel Savings",
                bank = "Your Bank",
                purpose = "Vacation fund",
                monthlyFlow = "+$117"
            )
        }
    }
}

@Composable
private fun AccountItem(
    name: String,
    bank: String,
    purpose: String,
    monthlyFlow: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "$bank • $purpose",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = monthlyFlow,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (monthlyFlow.startsWith("+")) Color(0xFF28a745) else Color(0xFFdc3545)
        )
    }
}

// Sample data
private data class SampleGoal(
    val title: String,
    val description: String,
    val current: String,
    val target: String,
    val progress: Float,
    val monthlyTarget: String,
    val color: Color,
    val icon: String
)

private val sampleGoals = listOf(
    SampleGoal(
        title = "Emergency Fund",
        description = "High-Yield Savings",
        current = "$0",
        target = "$16,000",
        progress = 0f,
        monthlyTarget = "$800",
        color = Color(0xFF28a745),
        icon = "🚨"
    ),
    SampleGoal(
        title = "Roth IRA",
        description = "Tax-free retirement",
        current = "$0",
        target = "$7,000",
        progress = 0f,
        monthlyTarget = "$583",
        color = Color(0xFF6f42c1),
        icon = "👴"
    ),
    SampleGoal(
        title = "Investment Portfolio",
        description = "Robinhood ETFs",
        current = "$0",
        target = "Flexible",
        progress = 0f,
        monthlyTarget = "$400",
        color = Color(0xFF17a2b8),
        icon = "📊"
    ),
    SampleGoal(
        title = "Travel Fund",
        description = "Vacation savings",
        current = "$0",
        target = "$1,400",
        progress = 0f,
        monthlyTarget = "$117",
        color = Color(0xFF20c997),
        icon = "✈️"
    )
)
