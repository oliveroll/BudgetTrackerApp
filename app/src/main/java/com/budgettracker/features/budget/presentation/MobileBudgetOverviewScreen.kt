package com.budgettracker.features.budget.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.budgettracker.core.data.local.entities.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Mobile-first Budget Overview Screen matching user specifications
 * - Month navigation with chevrons
 * - Current balance card with breakdown
 * - Essential expenses with Fixed badges
 * - Subscriptions with billing details and reminders
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileBudgetOverviewScreen(
    viewModel: BudgetOverviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showEditBalanceDialog by viewModel.showEditBalanceDialog.collectAsState()
    
    // Local state for dialogs
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddSubscriptionDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<EssentialExpenseEntity?>(null) }
    var editingSubscription by remember { mutableStateOf<EnhancedSubscriptionEntity?>(null) }
    var deleteConfirmExpense by remember { mutableStateOf<EssentialExpenseEntity?>(null) }
    var deleteConfirmSubscription by remember { mutableStateOf<EnhancedSubscriptionEntity?>(null) }
    
    // Month navigation state
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "💰",
                            fontSize = 28.sp
                        )
                        Text(
                            text = "Budget",
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
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Month Navigation Header
            item {
                MonthNavigationHeader(
                    currentMonth = monthFormat.format(currentMonth.time),
                    onPreviousMonth = {
                        currentMonth = (currentMonth.clone() as Calendar).apply {
                            add(Calendar.MONTH, -1)
                        }
                    },
                    onNextMonth = {
                        currentMonth = (currentMonth.clone() as Calendar).apply {
                            add(Calendar.MONTH, 1)
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Current Balance Card
            item {
                CurrentBalanceCard(
                    balance = uiState.currentBalance,
                    expenses = uiState.dashboardSummary?.totalPaid ?: 635.0,
                    subscriptions = viewModel.getTotalMonthlySubscriptions(),
                    remaining = uiState.currentBalance - (uiState.dashboardSummary?.totalPaid ?: 0.0) - viewModel.getTotalMonthlySubscriptions(),
                    onEditBalance = { viewModel.showEditBalanceDialog() },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Essential Expenses Section
            item {
                EssentialExpensesCard(
                    expensesWithSpending = uiState.essentialExpensesWithSpending,
                    onAddExpense = { showAddExpenseDialog = true },
                    onEditExpense = { editingExpense = it.expense },
                    onDeleteExpense = { deleteConfirmExpense = it.expense },
                    onToggleFixed = { /* TODO: Implement fixed toggle */ },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Subscriptions Section
            item {
                SubscriptionsCard(
                    subscriptions = uiState.subscriptions.filter { it.active },
                    totalMonthly = viewModel.getTotalMonthlySubscriptions(),
                    onAddSubscription = { showAddSubscriptionDialog = true },
                    onEditSubscription = { editingSubscription = it },
                    onDeleteSubscription = { deleteConfirmSubscription = it },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
    
    // Dialogs
    if (showEditBalanceDialog) {
        EditBalanceDialog(
            currentBalance = uiState.currentBalance,
            onConfirm = { newBalance -> viewModel.updateBalance(newBalance) },
            onDismiss = { viewModel.hideEditBalanceDialog() }
        )
    }
    
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onConfirm = { name, category, amount, dueDay ->
                viewModel.addEssentialExpense(name, category, amount, dueDay)
                showAddExpenseDialog = false
            },
            onDismiss = { showAddExpenseDialog = false }
        )
    }
    
    editingExpense?.let { expense ->
        EditExpenseDialog(
            expense = expense,
            onConfirm = { name, category, amount, dueDay ->
                viewModel.updateEssentialExpense(expense.id, name, category, amount, dueDay)
                editingExpense = null
            },
            onDismiss = { editingExpense = null }
        )
    }
    
    if (showAddSubscriptionDialog) {
        AddSubscriptionDialog(
            onConfirm = { name, amount, frequency, nextBillingDate, iconEmoji ->
                viewModel.addSubscription(name, amount, frequency, nextBillingDate, "Entertainment", iconEmoji)
                showAddSubscriptionDialog = false
            },
            onDismiss = { showAddSubscriptionDialog = false }
        )
    }
    
    editingSubscription?.let { subscription ->
        EditSubscriptionDialog(
            subscription = subscription,
            onConfirm = { name, amount, frequency, nextBillingDate, iconEmoji ->
                viewModel.updateSubscription(subscription.id, name, amount, frequency, nextBillingDate, iconEmoji)
                editingSubscription = null
            },
            onDismiss = { editingSubscription = null }
        )
    }
    
    deleteConfirmExpense?.let { expense ->
        ConfirmDeleteDialog(
            title = "Delete Expense",
            message = "Are you sure you want to delete '${expense.name}'?",
            onConfirm = {
                viewModel.deleteEssentialExpense(expense.id)
                deleteConfirmExpense = null
            },
            onDismiss = { deleteConfirmExpense = null }
        )
    }
    
    // Delete Subscription Confirmation Dialog
    deleteConfirmSubscription?.let { subscription ->
        ConfirmDeleteDialog(
            title = "Delete Subscription",
            message = "Are you sure you want to delete '${subscription.name}'?",
            onConfirm = {
                viewModel.deleteSubscription(subscription.id)
                deleteConfirmSubscription = null
            },
            onDismiss = { deleteConfirmSubscription = null }
        )
    }
}

@Composable
private fun MonthNavigationHeader(
    currentMonth: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "Previous Month",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Text(
                    text = currentMonth,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                IconButton(onClick = onNextMonth) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Next Month",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Text(
                text = "Budget Overview",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun CurrentBalanceCard(
    balance: Double,
    expenses: Double,
    subscriptions: Double,
    remaining: Double,
    onEditBalance: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header with icon and edit button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = Color(0xFF28a745),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Current Balance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                IconButton(onClick = onEditBalance, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Balance",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Large balance amount
            Text(
                text = "$${String.format("%.2f", balance)}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF28a745),
                fontSize = 42.sp
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Two-column breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left column
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Expenses",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "-$${String.format("%.2f", expenses)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFdc3545)
                    )
                }
                
                // Right column
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Subscriptions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "-$${String.format("%.2f", subscriptions)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFdc3545)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))
            
            // Remaining row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Remaining",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$${String.format("%.2f", remaining)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (remaining >= 0) Color(0xFF28a745) else Color(0xFFdc3545)
                )
            }
        }
    }
}

@Composable
private fun EssentialExpensesCard(
    expensesWithSpending: List<com.budgettracker.core.data.repository.EssentialExpenseWithSpending>,
    onAddExpense: () -> Unit,
    onEditExpense: (com.budgettracker.core.data.repository.EssentialExpenseWithSpending) -> Unit,
    onDeleteExpense: (com.budgettracker.core.data.repository.EssentialExpenseWithSpending) -> Unit,
    onToggleFixed: (com.budgettracker.core.data.repository.EssentialExpenseWithSpending) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = Color(0xFF6f42c1),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Essential Expenses",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                IconButton(onClick = onAddExpense, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Expense",
                        tint = Color(0xFF6f42c1)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Expense items
            if (expensesWithSpending.isNotEmpty()) {
                expensesWithSpending.forEach { expenseData ->
                    EssentialExpenseItem(
                        expenseData = expenseData,
                        onEdit = { onEditExpense(expenseData) },
                        onDelete = { onDeleteExpense(expenseData) },
                        onToggleFixed = { onToggleFixed(expenseData) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No expenses yet. Tap + to add one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EssentialExpenseItem(
    expenseData: com.budgettracker.core.data.repository.EssentialExpenseWithSpending,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFixed: () -> Unit
) {
    val expense = expenseData.expense
    val progress = (expenseData.spendingPercentage / 100f).coerceIn(0f, 1f)
    val progressColor = when {
        expenseData.isOverBudget -> Color(0xFFdc3545) // Red
        progress > 0.9f -> Color(0xFFffc107) // Yellow/Orange
        else -> Color(0xFF28a745) // Green
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        // Header row: Name, badge, and actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = expense.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (expense.dueDay != null) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "Fixed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFdc3545),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Spending info row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = expense.category.displayName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$${String.format("%.2f", expenseData.actualSpending)} / $${String.format("%.2f", expense.plannedAmount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = progressColor
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(progressColor, RoundedCornerShape(4.dp))
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Remaining amount
        Text(
            text = if (expenseData.remainingBudget >= 0) {
                "Remaining: $${String.format("%.2f", expenseData.remainingBudget)}"
            } else {
                "Over budget: $${String.format("%.2f", -expenseData.remainingBudget)}"
            },
            style = MaterialTheme.typography.labelSmall,
            color = if (expenseData.remainingBudget >= 0) Color(0xFF28a745) else Color(0xFFdc3545),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SubscriptionsCard(
    subscriptions: List<EnhancedSubscriptionEntity>,
    totalMonthly: Double,
    onAddSubscription: () -> Unit,
    onEditSubscription: (EnhancedSubscriptionEntity) -> Unit,
    onDeleteSubscription: (EnhancedSubscriptionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Subscriptions,
                        contentDescription = null,
                        tint = Color(0xFF6f42c1),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Subscriptions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                IconButton(onClick = onAddSubscription, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Subscription",
                        tint = Color(0xFF6f42c1)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Subscription items
            if (subscriptions.isNotEmpty()) {
                subscriptions.forEach { subscription ->
                    SubscriptionItem(
                        subscription = subscription,
                        onEdit = { onEditSubscription(subscription) },
                        onDelete = { onDeleteSubscription(subscription) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))
                
                // Total row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Monthly",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$${String.format("%.2f", totalMonthly)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6f42c1)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No subscriptions yet. Tap + to add one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SubscriptionItem(
    subscription: EnhancedSubscriptionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val daysUntil = subscription.getDaysUntilBilling()
    val reminderText = when {
        daysUntil < 0 -> "Overdue"
        daysUntil == 0 -> "Due today"
        daysUntil <= 7 -> "$daysUntil day${if (daysUntil > 1) "s" else ""} left"
        else -> "${daysUntil}d reminder"
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Left side: Details
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = subscription.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "active",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Recurring",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Next billing: ${dateFormat.format(Date(subscription.nextBillingDate))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "(${reminderText})",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (daysUntil <= 3) Color(0xFFff6b6b) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Right side: Price and actions
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format("%.2f", subscription.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "/monthly",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (daysUntil <= 7 && daysUntil >= 0) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Reminder active",
                        tint = Color(0xFFff9800),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFdc3545),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// Dialog components
@Composable
private fun EditBalanceDialog(
    currentBalance: Double,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var balanceText by remember { mutableStateOf(String.format("%.2f", currentBalance)) }
    var isError by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Edit Current Balance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { 
                        balanceText = it
                        isError = it.toDoubleOrNull() == null
                    },
                    label = { Text("Balance") },
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = isError,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            balanceText.toDoubleOrNull()?.let { onConfirm(it) }
                        },
                        enabled = !isError && balanceText.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseDialog(
    onConfirm: (String, ExpenseCategory, Double, Int?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.OTHER) }
    var amount by remember { mutableStateOf("") }
    var dueDay by remember { mutableStateOf("") }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var isFixed by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Add Essential Expense",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Expense Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Category dropdown
                Box {
                    OutlinedTextField(
                        value = "${selectedCategory.iconEmoji} ${selectedCategory.displayName}",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            IconButton(onClick = { showCategoryDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCategoryDropdown = true }
                    )
                    
                    DropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        ExpenseCategory.values().forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Row {
                                        Text(category.iconEmoji)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(category.displayName)
                                    }
                                },
                                onClick = {
                                    selectedCategory = category
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isFixed,
                        onCheckedChange = { isFixed = it }
                    )
                    Text("Fixed expense (recurring)")
                }
                
                if (isFixed) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = dueDay,
                        onValueChange = { dueDay = it },
                        label = { Text("Due day (1-31)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedAmount = amount.toDoubleOrNull()
                            val parsedDueDay = if (isFixed) dueDay.toIntOrNull() else null
                            if (name.isNotBlank() && parsedAmount != null) {
                                onConfirm(name, selectedCategory, parsedAmount, parsedDueDay)
                            }
                        },
                        enabled = name.isNotBlank() && amount.toDoubleOrNull() != null
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditExpenseDialog(
    expense: EssentialExpenseEntity,
    onConfirm: (String, ExpenseCategory, Double, Int?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(expense.name) }
    var selectedCategory by remember { mutableStateOf(expense.category) }
    var amount by remember { mutableStateOf(expense.plannedAmount.toString()) }
    var dueDay by remember { mutableStateOf(expense.dueDay?.toString() ?: "") }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var isFixed by remember { mutableStateOf(expense.dueDay != null) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Edit Essential Expense",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Expense Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Box {
                    OutlinedTextField(
                        value = "${selectedCategory.iconEmoji} ${selectedCategory.displayName}",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            IconButton(onClick = { showCategoryDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCategoryDropdown = true }
                    )
                    
                    DropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        ExpenseCategory.values().forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Row {
                                        Text(category.iconEmoji)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(category.displayName)
                                    }
                                },
                                onClick = {
                                    selectedCategory = category
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isFixed,
                        onCheckedChange = { isFixed = it }
                    )
                    Text("Fixed expense (recurring)")
                }
                
                if (isFixed) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = dueDay,
                        onValueChange = { dueDay = it },
                        label = { Text("Due day (1-31)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedAmount = amount.toDoubleOrNull()
                            val parsedDueDay = if (isFixed) dueDay.toIntOrNull() else null
                            if (name.isNotBlank() && parsedAmount != null) {
                                onConfirm(name, selectedCategory, parsedAmount, parsedDueDay)
                            }
                        },
                        enabled = name.isNotBlank() && amount.toDoubleOrNull() != null
                    ) {
                        Text("Update")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSubscriptionDialog(
    onConfirm: (String, Double, BillingFrequency, Long, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedFrequency by remember { mutableStateOf(BillingFrequency.MONTHLY) }
    var nextBillingDate by remember { mutableStateOf(Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 30) }.timeInMillis) }
    var iconEmoji by remember { mutableStateOf("💳") }
    var showFrequencyDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = nextBillingDate
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Add Subscription",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Service Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Frequency dropdown
                Box {
                    OutlinedTextField(
                        value = selectedFrequency.displayName,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Billing Frequency") },
                        trailingIcon = {
                            IconButton(onClick = { showFrequencyDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showFrequencyDropdown = true }
                    )
                    
                    DropdownMenu(
                        expanded = showFrequencyDropdown,
                        onDismissRequest = { showFrequencyDropdown = false }
                    ) {
                        BillingFrequency.values().forEach { frequency ->
                            DropdownMenuItem(
                                text = { Text(frequency.displayName) },
                                onClick = {
                                    selectedFrequency = frequency
                                    showFrequencyDropdown = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = iconEmoji,
                    onValueChange = { if (it.length <= 2) iconEmoji = it },
                    label = { Text("Icon (emoji)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Next billing: ${dateFormat.format(Date(nextBillingDate))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedAmount = amount.toDoubleOrNull()
                            if (name.isNotBlank() && parsedAmount != null) {
                                onConfirm(name, parsedAmount, selectedFrequency, nextBillingDate, iconEmoji)
                            }
                        },
                        enabled = name.isNotBlank() && amount.toDoubleOrNull() != null
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            nextBillingDate = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditSubscriptionDialog(
    subscription: EnhancedSubscriptionEntity,
    onConfirm: (String, Double, BillingFrequency, Long, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(subscription.name) }
    var amount by remember { mutableStateOf(subscription.amount.toString()) }
    var selectedFrequency by remember { mutableStateOf(subscription.frequency) }
    var nextBillingDate by remember { mutableStateOf(subscription.nextBillingDate) }
    var iconEmoji by remember { mutableStateOf(subscription.iconEmoji ?: "💳") }
    var showFrequencyDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = nextBillingDate
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Edit Subscription",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Service Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Box {
                    OutlinedTextField(
                        value = selectedFrequency.displayName,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Billing Frequency") },
                        trailingIcon = {
                            IconButton(onClick = { showFrequencyDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showFrequencyDropdown = true }
                    )
                    
                    DropdownMenu(
                        expanded = showFrequencyDropdown,
                        onDismissRequest = { showFrequencyDropdown = false }
                    ) {
                        BillingFrequency.values().forEach { frequency ->
                            DropdownMenuItem(
                                text = { Text(frequency.displayName) },
                                onClick = {
                                    selectedFrequency = frequency
                                    showFrequencyDropdown = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = iconEmoji,
                    onValueChange = { if (it.length <= 2) iconEmoji = it },
                    label = { Text("Icon (emoji)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Date picker card
                Card(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Next Billing Date",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dateFormat.format(Date(nextBillingDate)),
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = "Select Date",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedAmount = amount.toDoubleOrNull()
                            if (name.isNotBlank() && parsedAmount != null) {
                                onConfirm(name, parsedAmount, selectedFrequency, nextBillingDate, iconEmoji)
                            }
                        },
                        enabled = name.isNotBlank() && amount.toDoubleOrNull() != null
                    ) {
                        Text("Update")
                    }
                }
            }
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            nextBillingDate = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    }
}

@Composable
private fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFdc3545)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFdc3545)
                        )
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

