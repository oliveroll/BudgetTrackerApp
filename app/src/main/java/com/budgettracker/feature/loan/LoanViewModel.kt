package com.budgettracker.feature.loan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgettracker.core.domain.model.Loan
import com.budgettracker.core.domain.model.LoanPayment
import com.budgettracker.core.domain.model.LoanSummary
import com.budgettracker.core.domain.usecase.AddLoanPayment
import com.budgettracker.core.domain.usecase.GetLoanById
import com.budgettracker.core.domain.usecase.GetLoanSummary
import com.budgettracker.core.domain.usecase.InitializeGermanLoanPlan
import com.budgettracker.core.domain.repository.LoanRepository
import com.budgettracker.core.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class LoanViewModel @Inject constructor(
    private val loanRepository: LoanRepository,
    private val getLoanById: GetLoanById,
    private val getLoanSummary: GetLoanSummary,
    private val addLoanPayment: AddLoanPayment,
    private val initializeGermanLoanPlan: InitializeGermanLoanPlan
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoanUiState())
    val uiState: StateFlow<LoanUiState> = _uiState.asStateFlow()
    
    private val _currentLoanId = MutableStateFlow<String?>(null)
    
    fun loadLoan(loanId: String) {
        _currentLoanId.value = loanId
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                combine(
                    getLoanById.asFlow(loanId),
                    loanRepository.getPaymentsByLoanId(loanId)
                ) { loan, payments ->
                    Pair(loan, payments)
                }.collect { (loan, payments) ->
                    if (loan != null) {
                        val kpis = calculateKPIs(loan, payments)
                        val progress = calculateProgress(loan, payments)
                        val nextPaymentInfo = calculateNextPayment(loan, payments)
                        
                        _uiState.value = _uiState.value.copy(
                            loan = loan,
                            payments = payments,
                            kpis = kpis,
                            progress = progress,
                            nextPaymentDate = nextPaymentInfo.first,
                            isOverdue = nextPaymentInfo.second,
                            isLoading = false,
                            error = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Loan not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load loan: ${e.message}"
                )
            }
        }
    }
    
    fun loadLoanSummary(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = getLoanSummary(userId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        summary = result.data,
                        isLoading = false,
                        error = null
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }
    
    fun addPayment(payment: LoanPayment) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = addLoanPayment(payment)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                    _currentLoanId.value?.let { loadLoan(it) }
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }
    
    fun initializeGermanLoan(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = initializeGermanLoanPlan(userId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                    loadLoan(result.data)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    private fun calculateKPIs(loan: Loan, payments: List<LoanPayment>): LoanKPIs {
        val totalPaid = payments.sumOf { it.amount }
        val totalInterestPaid = payments.sumOf { it.interestAmount }
        val totalPrincipalPaid = payments.sumOf { it.principalAmount }
        
        val monthsRemaining = if (loan.monthlyPayment > 0 && loan.remainingAmount > 0) {
            (loan.remainingAmount / loan.monthlyPayment).toLong().coerceAtLeast(1L)
        } else 0L
        
        val projectedTotalInterest = totalInterestPaid + (monthsRemaining * (loan.remainingAmount * (loan.interestRate / 12 / 100)))
        val originalPlanTotal = 14200.29
        val acceleratedPlanTotal = 10711.70
        val interestSaved = originalPlanTotal - acceleratedPlanTotal
        
        return LoanKPIs(
            totalPaid = totalPaid,
            totalInterestPaid = totalInterestPaid,
            totalPrincipalPaid = totalPrincipalPaid,
            monthsRemaining = monthsRemaining,
            projectedTotalInterest = projectedTotalInterest,
            interestSavedVsOriginal = interestSaved,
            timeSavedMonths = 105L
        )
    }
    
    private fun calculateProgress(loan: Loan, payments: List<LoanPayment>): LoanProgress {
        val totalPrincipalPaid = payments.sumOf { it.principalAmount }
        val progressPercentage = if (loan.originalAmount > 0) {
            ((totalPrincipalPaid / loan.originalAmount) * 100).toFloat()
        } else 0f
        
        return LoanProgress(
            progressPercentage = progressPercentage,
            remainingBalance = loan.remainingAmount,
            originalAmount = loan.originalAmount,
            remainingBalanceUSD = loan.remainingAmount * 1.05
        )
    }
    
    private fun calculateNextPayment(loan: Loan, payments: List<LoanPayment>): Pair<Date?, Boolean> {
        val lastPayment = payments.maxByOrNull { it.paymentDate }
        val calendar = Calendar.getInstance()
        
        val nextPaymentDate = if (lastPayment != null) {
            calendar.time = lastPayment.paymentDate
            calendar.add(Calendar.MONTH, 1)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.time
        } else {
            calendar.add(Calendar.MONTH, 1)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.time
        }
        
        val today = Calendar.getInstance().time
        val isOverdue = today.after(nextPaymentDate) && 
                        (today.time - nextPaymentDate.time) > (5 * 24 * 60 * 60 * 1000L)
        
        return Pair(nextPaymentDate, isOverdue)
    }
}

data class LoanUiState(
    val loan: Loan? = null,
    val payments: List<LoanPayment> = emptyList(),
    val summary: LoanSummary? = null,
    val kpis: LoanKPIs? = null,
    val progress: LoanProgress? = null,
    val nextPaymentDate: Date? = null,
    val isOverdue: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class LoanKPIs(
    val totalPaid: Double,
    val totalInterestPaid: Double,
    val totalPrincipalPaid: Double,
    val monthsRemaining: Long,
    val projectedTotalInterest: Double,
    val interestSavedVsOriginal: Double,
    val timeSavedMonths: Long
)

data class LoanProgress(
    val progressPercentage: Float,
    val remainingBalance: Double,
    val originalAmount: Double,
    val remainingBalanceUSD: Double
)
