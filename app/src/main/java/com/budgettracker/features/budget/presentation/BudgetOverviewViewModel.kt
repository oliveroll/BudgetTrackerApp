package com.budgettracker.features.budget.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgettracker.core.data.local.dao.DashboardSummary
import com.budgettracker.core.data.local.dao.TimelineItem
import com.budgettracker.core.data.local.entities.*
import com.budgettracker.core.data.repository.BudgetOverviewRepository
import com.budgettracker.core.data.repository.EssentialExpenseWithSpending
import com.budgettracker.core.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * ViewModel for Budget Overview Screen
 * Integrates with existing architecture and provides comprehensive financial overview
 */
@HiltViewModel
class BudgetOverviewViewModel @Inject constructor(
    private val repository: BudgetOverviewRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BudgetOverviewUiState())
    val uiState: StateFlow<BudgetOverviewUiState> = _uiState.asStateFlow()
    
    private val _showEditBalanceDialog = MutableStateFlow(false)
    val showEditBalanceDialog: StateFlow<Boolean> = _showEditBalanceDialog.asStateFlow()
    
    // Track selected month/year for filtering
    private var selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH)
    private var selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR)
    
    init {
        loadBudgetData()
        observeDataChanges()
    }
    
    /**
     * Set the selected month and reload data for that period
     */
    fun setSelectedMonth(month: Int, year: Int) {
        selectedMonth = month
        selectedYear = year
        loadBudgetData()
    }
    
    private fun loadBudgetData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Format period as YYYY-MM for repository
                val period = "$selectedYear-${(selectedMonth + 1).toString().padStart(2, '0')}"
                
                // Load all data concurrently with selected period
                val balanceResult = repository.getCurrentBalance()
                val essentialsResult = repository.getEssentialExpenses(period)
                val essentialsWithSpendingResult = repository.getEssentialExpensesWithSpending(period)
                val subscriptionsResult = repository.getActiveSubscriptions()
                val paychecksResult = repository.getUpcomingPaychecks()
                val timelineResult = repository.getUpcomingTimeline()
                val summaryResult = repository.getDashboardSummary()
                
                // Process results
                val balance = when (balanceResult) {
                    is Result.Success -> balanceResult.data
                    is Result.Error -> 0.0
                }
                
                val essentials = when (essentialsResult) {
                    is Result.Success -> essentialsResult.data
                    is Result.Error -> emptyList()
                }
                
                val essentialsWithSpending = when (essentialsWithSpendingResult) {
                    is Result.Success -> essentialsWithSpendingResult.data
                    is Result.Error -> emptyList()
                }
                
                val subscriptions = when (subscriptionsResult) {
                    is Result.Success -> subscriptionsResult.data
                    is Result.Error -> emptyList()
                }
                
                val paychecks = when (paychecksResult) {
                    is Result.Success -> paychecksResult.data
                    is Result.Error -> emptyList()
                }
                
                val timeline = when (timelineResult) {
                    is Result.Success -> timelineResult.data
                    is Result.Error -> emptyList()
                }
                
                val summary = when (summaryResult) {
                    is Result.Success -> summaryResult.data
                    is Result.Error -> null
                }
                
                // Update UI state
                _uiState.value = BudgetOverviewUiState(
                    isLoading = false,
                    currentBalance = balance,
                    essentialExpenses = essentials,
                    essentialExpensesWithSpending = essentialsWithSpending,
                    subscriptions = subscriptions,
                    upcomingPaychecks = paychecks,
                    upcomingTimeline = timeline,
                    dashboardSummary = summary,
                    monthlyIncome = 5191.32, // From requirements
                    lastUpdated = System.currentTimeMillis()
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load budget data: ${e.message}"
                )
            }
        }
    }
    
    private fun observeDataChanges() {
        viewModelScope.launch {
            // Observe balance changes (not month-specific)
            repository.getBalanceFlow()
                .collect { balance ->
                    _uiState.value = _uiState.value.copy(currentBalance = balance)
                }
        }
        
        viewModelScope.launch {
            // Observe subscriptions changes (not month-specific)
            repository.getActiveSubscriptionsFlow()
                .collect { subscriptions ->
                    _uiState.value = _uiState.value.copy(subscriptions = subscriptions)
                }
        }
        
        // Note: Month-specific data (essentials, spending) is loaded via loadBudgetData()
        // when the user changes months. Real-time observation would overwrite selected month data.
    }
    
    private fun getCurrentPeriod(): String {
        return "$selectedYear-${(selectedMonth + 1).toString().padStart(2, '0')}"
    }
    
    fun refreshData() {
        loadBudgetData()
    }
    
    fun showEditBalanceDialog() {
        _showEditBalanceDialog.value = true
    }
    
    fun hideEditBalanceDialog() {
        _showEditBalanceDialog.value = false
    }
    
    fun updateBalance(newBalance: Double) {
        viewModelScope.launch {
            when (val result = repository.updateBalance(newBalance, "user")) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        currentBalance = newBalance,
                        lastUpdated = System.currentTimeMillis()
                    )
                    hideEditBalanceDialog()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
            }
        }
    }
    
    fun markEssentialPaid(expenseId: String, actualAmount: Double? = null) {
        viewModelScope.launch {
            when (val result = repository.markEssentialPaid(expenseId, actualAmount)) {
                is Result.Success -> {
                    // UI will update automatically through Flow observation
                    // Show success message or animation
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Expense marked as paid",
                        lastUpdated = System.currentTimeMillis()
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
            }
        }
    }
    
    fun markPaycheckDeposited(paycheckId: String) {
        viewModelScope.launch {
            when (val result = repository.markPaycheckDeposited(paycheckId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Paycheck marked as deposited",
                        lastUpdated = System.currentTimeMillis()
                    )
                    // Refresh to get updated data
                    loadBudgetData()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
            }
        }
    }
    
    fun addEssentialExpense(
        name: String,
        category: ExpenseCategory,
        plannedAmount: Double,
        dueDay: Int?
    ) {
        viewModelScope.launch {
            when (val result = repository.addEssentialExpense(name, category, plannedAmount, dueDay)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Essential expense added",
                        lastUpdated = System.currentTimeMillis()
                    )
                    // Reload data to refresh spending calculations
                    loadBudgetData()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
            }
        }
    }
    
    fun updateEssentialExpense(
        expenseId: String,
        name: String,
        category: ExpenseCategory,
        plannedAmount: Double,
        dueDay: Int?
    ) {
        viewModelScope.launch {
            when (val result = repository.updateEssentialExpense(expenseId, name, category, plannedAmount, dueDay)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Essential expense updated",
                        lastUpdated = System.currentTimeMillis()
                    )
                    // Reload data to refresh spending calculations
                    loadBudgetData()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
            }
        }
    }
    
    fun deleteEssentialExpense(expenseId: String) {
        viewModelScope.launch {
            when (val result = repository.deleteEssentialExpense(expenseId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Essential expense deleted",
                        lastUpdated = System.currentTimeMillis()
                    )
                    // Reload data to refresh spending calculations
                    loadBudgetData()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
            }
        }
    }
    
    fun addSubscription(
        name: String,
        amount: Double,
        frequency: BillingFrequency,
        nextBillingDate: Long,
        category: String,
        iconEmoji: String?
    ) {
        viewModelScope.launch {
            when (val result = repository.addSubscription(
                name, amount, frequency, nextBillingDate, category, iconEmoji = iconEmoji
            )) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Subscription added",
                        lastUpdated = System.currentTimeMillis()
                    )
                    // Reload data to refresh
                    loadBudgetData()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
            }
        }
    }
    
    fun updateSubscription(
        subscriptionId: String,
        name: String,
        amount: Double,
        frequency: BillingFrequency,
        nextBillingDate: Long,
        iconEmoji: String?
    ) {
        viewModelScope.launch {
            when (val result = repository.updateSubscription(subscriptionId, name, amount, frequency, nextBillingDate, iconEmoji)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Subscription updated",
                        lastUpdated = System.currentTimeMillis()
                    )
                    // Reload data to refresh
                    loadBudgetData()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
            }
        }
    }
    
    fun deleteSubscription(subscriptionId: String) {
        viewModelScope.launch {
            when (val result = repository.deleteSubscription(subscriptionId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Subscription deleted",
                        lastUpdated = System.currentTimeMillis()
                    )
                    // Reload data to refresh
                    loadBudgetData()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
            }
        }
    }
    
    fun addPaycheck(date: Long, grossAmount: Double, netAmount: Double) {
        viewModelScope.launch {
            when (val result = repository.addPaycheck(date, grossAmount, netAmount)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Paycheck added",
                        lastUpdated = System.currentTimeMillis()
                    )
                    loadBudgetData() // Refresh to show new paycheck
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
    
    // Seed sample data for demonstration
    fun seedSampleData() {
        viewModelScope.launch {
            try {
                // Add sample essential expenses
                repository.addEssentialExpense("Rent", ExpenseCategory.RENT, 708.95, 31)
                repository.addEssentialExpense("Electric Bill", ExpenseCategory.UTILITIES, 85.0, 15)
                repository.addEssentialExpense("Groceries", ExpenseCategory.GROCERIES, 400.0, null)
                repository.addEssentialExpense("Phone Plan", ExpenseCategory.PHONE, 150.0, 20)
                
                // Add sample subscriptions
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_MONTH, 15) // Spotify in 15 days
                repository.addSubscription(
                    name = "Spotify Premium", 
                    amount = 11.99, 
                    frequency = BillingFrequency.MONTHLY, 
                    nextBillingDate = calendar.timeInMillis, 
                    category = "Entertainment",
                    reminderDaysBefore = listOf(3, 1),
                    iconEmoji = "🎵"
                )
                
                calendar.add(Calendar.DAY_OF_MONTH, 20) // Phone plan renewal
                repository.addSubscription(
                    name = "Phone Plan", 
                    amount = 150.0, 
                    frequency = BillingFrequency.SEMI_ANNUAL,
                    nextBillingDate = calendar.timeInMillis, 
                    category = "Utilities",
                    reminderDaysBefore = listOf(7, 3),
                    iconEmoji = "📱"
                )
                
                // Add sample paychecks
                calendar.set(2024, Calendar.OCTOBER, 31) // Oct 31 paycheck
                repository.addPaycheck(calendar.timeInMillis, 4000.0, 2595.66)
                
                calendar.set(2024, Calendar.NOVEMBER, 15) // Nov 15 paycheck
                repository.addPaycheck(calendar.timeInMillis, 4000.0, 2595.66)
                
                // Set initial balance
                repository.updateBalance(393.97, "initial")
                
                _uiState.value = _uiState.value.copy(
                    successMessage = "Sample data loaded successfully"
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to seed sample data: ${e.message}"
                )
            }
        }
    }
    
    // Helper methods for UI
    fun getHealthScore(): Int {
        val summary = _uiState.value.dashboardSummary ?: return 85
        val totalExpenses = (summary.totalPlanned ?: 0.0) + getTotalMonthlySubscriptions()
        val expenseRatio = totalExpenses / _uiState.value.monthlyIncome
        
        return when {
            expenseRatio <= 0.5 -> 100 // Excellent
            expenseRatio <= 0.7 -> 80  // Good
            expenseRatio <= 0.85 -> 60 // Fair
            else -> 30 // Needs attention
        }
    }
    
    fun getTotalMonthlySubscriptions(): Double {
        return _uiState.value.subscriptions.sumOf { it.getMonthlyCost() }
    }
    
    fun getNetRemaining(): Double {
        val summary = _uiState.value.dashboardSummary ?: return 0.0
        val totalExpenses = (summary.totalPlanned ?: 0.0) + getTotalMonthlySubscriptions()
        return _uiState.value.monthlyIncome - totalExpenses
    }
    
    fun getFormattedLastUpdated(): String {
        val format = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
        return format.format(Date(_uiState.value.lastUpdated))
    }
}

/**
 * UI State for Budget Overview Screen
 */
data class BudgetOverviewUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val currentBalance: Double = 0.0,
    val monthlyIncome: Double = 5191.32,
    val essentialExpenses: List<EssentialExpenseEntity> = emptyList(),
    val essentialExpensesWithSpending: List<EssentialExpenseWithSpending> = emptyList(),
    val subscriptions: List<EnhancedSubscriptionEntity> = emptyList(),
    val upcomingPaychecks: List<PaycheckEntity> = emptyList(),
    val upcomingTimeline: List<TimelineItem> = emptyList(),
    val dashboardSummary: DashboardSummary? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)