package com.budgettracker.features.savings.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    onNavigateToAddGoal: () -> Unit = {}
) {
    var selectedMonth by remember { mutableStateOf(0) } // 0 = Dec 2025, 11 = Nov 2026
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var isDataLoaded by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isDataLoaded = true
    }
    
    val loanData = remember { createLoanPayoffSchedule() }
    val currentData = loanData.schedule[selectedMonth]
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Financial Goals",
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }
            
            // Debt Freedom Progress Header
            item {
                DebtFreedomHeader(
                    loanData = loanData,
                    currentMonth = selectedMonth,
                    isVisible = isDataLoaded,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Interactive Payoff Timeline
            item {
                PayoffTimelineCard(
                    loanData = loanData,
                    selectedMonth = selectedMonth,
                    onMonthSelected = { selectedMonth = it },
                    onShowDetails = { showDetailsDialog = true },
                    isVisible = isDataLoaded,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Current Month Details
            item {
                CurrentMonthBreakdownCard(
                    monthData = currentData,
                    isVisible = isDataLoaded,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Future Freedom Plan
            item {
                FutureFreedomCard(
                    isVisible = isDataLoaded,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Quick Stats
            item {
                QuickStatsRow(
                    loanData = loanData,
                    isVisible = isDataLoaded,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
    
    if (showDetailsDialog) {
        FullScheduleDialog(
            loanData = loanData,
            onDismiss = { showDetailsDialog = false }
        )
    }
}

@Composable
private fun DebtFreedomHeader(
    loanData: LoanPayoffData,
    currentMonth: Int,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = if (isVisible) (currentMonth + 1) / 12f else 0f,
        animationSpec = tween(durationMillis = 1500, easing = EaseOutCubic),
        label = "progress_anim"
    )
    
    val currentBalance = loanData.schedule[currentMonth].remainingBalance
    val animatedBalance by animateFloatAsState(
        targetValue = if (isVisible) currentBalance.toFloat() else loanData.startingBalance.toFloat(),
        animationSpec = tween(durationMillis = 1500, easing = EaseOutCubic),
        label = "balance_anim"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFf093fb),
                            Color(0xFFf5576c)
                        )
                    )
                )
                .padding(28.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = "Debt Freedom Journey",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 24.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Progress Ring
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DebtProgressRing(
                        progress = progress,
                        modifier = Modifier.size(180.dp)
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 48.sp
                        )
                        Text(
                            text = "Paid Off",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 16.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Remaining Balance",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "€${String.format("%,.2f", animatedBalance)}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 32.sp
                        )
                    }
                    
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.25f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Freedom Date",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Nov 2026",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DebtProgressRing(
    progress: Float,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val strokeWidth = 20f
        val radius = (size.minDimension - strokeWidth) / 2
        val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
        
        // Background ring
        drawCircle(
            color = Color.White.copy(alpha = 0.3f),
            radius = radius,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        
        // Progress ring
        drawArc(
            color = Color.White,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(
                center.x - radius,
                center.y - radius
            ),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
    }
}

@Composable
private fun PayoffTimelineCard(
    loanData: LoanPayoffData,
    selectedMonth: Int,
    onMonthSelected: (Int) -> Unit,
    onShowDetails: () -> Unit,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Payoff Timeline",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                
                TextButton(onClick = onShowDetails) {
                    Text("View All")
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Month Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                loanData.schedule.forEachIndexed { index, monthData ->
                    MonthPill(
                        month = monthData.month,
                        isSelected = index == selectedMonth,
                        isPaid = index < selectedMonth,
                        onClick = { onMonthSelected(index) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Balance progress bar
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = (selectedMonth + 1) / 12f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF4facfe),
                                        Color(0xFF00f2fe)
                                    )
                                )
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Dec 2025",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Text(
                    text = "Nov 2026",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun MonthPill(
    month: String,
    isSelected: Boolean,
    isPaid: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isPaid -> Color(0xFF28a745)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = if (isSelected || isPaid) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isPaid) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = month,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun CurrentMonthBreakdownCard(
    monthData: MonthPayoffData,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedPayment by animateFloatAsState(
        targetValue = if (isVisible) monthData.payment.toFloat() else 0f,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "payment"
    )
    
    val animatedPrincipal by animateFloatAsState(
        targetValue = if (isVisible) monthData.principal.toFloat() else 0f,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "principal"
    )
    
    val animatedInterest by animateFloatAsState(
        targetValue = if (isVisible) monthData.interest.toFloat() else 0f,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "interest"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = monthData.month,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Payment breakdown
            PaymentBreakdownItem(
                label = "Payment",
                amount = animatedPayment.toDouble(),
                icon = Icons.Default.Payment,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PaymentBreakdownItem(
                label = "Principal",
                amount = animatedPrincipal.toDouble(),
                icon = Icons.Default.TrendingDown,
                color = Color(0xFF28a745)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PaymentBreakdownItem(
                label = "Interest",
                amount = animatedInterest.toDouble(),
                icon = Icons.Default.Percent,
                color = Color(0xFFffc107)
            )
        }
    }
}

@Composable
private fun PaymentBreakdownItem(
    label: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }
        
        Text(
            text = "€${String.format("%,.2f", amount)}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color,
            fontSize = 20.sp
        )
    }
}

@Composable
private fun FutureFreedomCard(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF667eea),
                            Color(0xFF764ba2)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Celebration,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = "After Debt Freedom",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Starting December 2026",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                FutureAllocationItem(
                    emoji = "🏦",
                    label = "Emergency Fund",
                    amount = "$1,000/mo",
                    description = "Build 6-month cushion"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                FutureAllocationItem(
                    emoji = "📈",
                    label = "Investments",
                    amount = "$2,083/mo",
                    description = "Roth IRA + ETFs"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                FutureAllocationItem(
                    emoji = "✈️",
                    label = "Travel & Fun",
                    amount = "$1,150/mo",
                    description = "Live your best life"
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Monthly Freedom",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    
                    Text(
                        text = "$4,233",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 28.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FutureAllocationItem(
    emoji: String,
    label: String,
    amount: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = emoji,
                fontSize = 28.sp
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    fontSize = 15.sp
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp
                )
            }
        }
        
        Text(
            text = amount,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 18.sp
        )
    }
}

@Composable
private fun QuickStatsRow(
    loanData: LoanPayoffData,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatCard(
            icon = "💰",
            label = "Total Interest",
            value = "€${String.format("%,.2f", loanData.totalInterest)}",
            color = Color(0xFFffc107),
            modifier = Modifier.weight(1f)
        )
        
        QuickStatCard(
            icon = "⏱️",
            label = "Time Saved",
            value = "8.75 yrs",
            color = Color(0xFF28a745),
            modifier = Modifier.weight(1f)
        )
        
        QuickStatCard(
            icon = "💸",
            label = "Interest Saved",
            value = "€3,367",
            color = Color(0xFF007bff),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickStatCard(
    icon: String,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                fontSize = 28.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullScheduleDialog(
    loanData: LoanPayoffData,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Complete Payoff Schedule",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(loanData.schedule.size) { index ->
                    val month = loanData.schedule[index]
                    ScheduleItem(monthData = month)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun ScheduleItem(monthData: MonthPayoffData) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = monthData.month,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Payment: €${String.format("%.2f", monthData.payment)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Balance: €${String.format("%,.2f", monthData.remainingBalance)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Data Models
data class LoanPayoffData(
    val startingBalance: Double,
    val totalInterest: Double,
    val schedule: List<MonthPayoffData>
)

data class MonthPayoffData(
    val month: String,
    val payment: Double,
    val interest: Double,
    val principal: Double,
    val remainingBalance: Double
)

private fun createLoanPayoffSchedule(): LoanPayoffData {
    val schedule = listOf(
        MonthPayoffData("Dec 2025", 900.00, 57.26, 842.74, 9474.90),
        MonthPayoffData("Jan 2026", 900.00, 52.58, 847.42, 8627.48),
        MonthPayoffData("Feb 2026", 900.00, 47.88, 852.12, 7775.36),
        MonthPayoffData("Mar 2026", 900.00, 43.16, 856.84, 6918.52),
        MonthPayoffData("Apr 2026", 900.00, 38.40, 861.60, 6056.92),
        MonthPayoffData("May 2026", 900.00, 33.62, 866.38, 5190.54),
        MonthPayoffData("Jun 2026", 900.00, 28.81, 871.19, 4319.35),
        MonthPayoffData("Jul 2026", 900.00, 23.98, 876.02, 3443.33),
        MonthPayoffData("Aug 2026", 900.00, 19.11, 880.89, 2562.44),
        MonthPayoffData("Sep 2026", 900.00, 14.22, 885.78, 1676.66),
        MonthPayoffData("Oct 2026", 900.00, 9.31, 890.69, 785.97),
        MonthPayoffData("Nov 2026", 790.33, 4.36, 785.97, 0.00)
    )
    
    return LoanPayoffData(
        startingBalance = 10317.64,
        totalInterest = 394.69,
        schedule = schedule
    )
}


