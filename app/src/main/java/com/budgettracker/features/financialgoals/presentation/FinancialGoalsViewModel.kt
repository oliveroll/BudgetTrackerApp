package com.budgettracker.features.financialgoals.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgettracker.core.data.repository.*
import com.budgettracker.core.domain.model.*
import com.budgettracker.core.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Debt Loan (Freedom Debt Journey) tab
 */
@HiltViewModel
class DebtJourneyViewModel @Inject constructor(
    private val repository: DebtLoanRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DebtJourneyUiState())
    val uiState: StateFlow<DebtJourneyUiState> = _uiState.asStateFlow()
    
    init {
        loadLoans()
    }
    
    fun loadLoans() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            repository.getActiveLoansFlow()
                .catch { e ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Failed to load loans: ${e.message}"
                    )}
                }
                .collect { loans ->
                    _uiState.update { it.copy(
                        loans = loans,
                        isLoading = false,
                        error = null
                    )}
                }
        }
    }
    
    fun selectLoan(loan: DebtLoan?) {
        _uiState.update { it.copy(selectedLoan = loan) }
    }
    
    fun addLoan(loan: DebtLoan) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.addLoan(loan)) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        showAddDialog = false
                    )}
                }
                is Result.Error -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = result.message
                    )}
                }
            }
        }
    }
    
    fun updateLoan(loan: DebtLoan) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.updateLoan(loan)) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        showEditDialog = false,
                        selectedLoan = null
                    )}
                }
                is Result.Error -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = result.message
                    )}
                }
            }
        }
    }
    
    fun deleteLoan(loanId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.deleteLoan(loanId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        selectedLoan = null
                    )}
                }
                is Result.Error -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = result.message
                    )}
                }
            }
        }
    }
    
    fun recordPayment(loanId: String, amount: Double, principalPaid: Double, interestPaid: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.recordPayment(loanId, amount, principalPaid, interestPaid)) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        showPaymentDialog = false
                    )}
                }
                is Result.Error -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = result.message
                    )}
                }
            }
        }
    }
    
    fun simulatePayment(loan: DebtLoan, newMonthlyPayment: Double) {
        val simulation = loan.simulatePayment(newMonthlyPayment)
        _uiState.update { it.copy(currentSimulation = simulation) }
    }
    
    fun toggleAddDialog() {
        _uiState.update { it.copy(showAddDialog = !it.showAddDialog) }
    }
    
    fun toggleEditDialog() {
        _uiState.update { it.copy(showEditDialog = !it.showEditDialog) }
    }
    
    fun togglePaymentDialog() {
        _uiState.update { it.copy(showPaymentDialog = !it.showPaymentDialog) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class DebtJourneyUiState(
    val loans: List<DebtLoan> = emptyList(),
    val selectedLoan: DebtLoan? = null,
    val currentSimulation: PaymentSimulation? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val showPaymentDialog: Boolean = false
)

/**
 * ViewModel for Roth IRA tab
 */
@HiltViewModel
class RothIRAViewModel @Inject constructor(
    private val repository: RothIRARepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RothIRAUiState())
    val uiState: StateFlow<RothIRAUiState> = _uiState.asStateFlow()
    
    init {
        loadIRAs()
    }
    
    fun loadIRAs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            repository.getActiveIRAsFlow()
                .catch { e ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Failed to load IRAs: ${e.message}"
                    )}
                }
                .collect { iras ->
                    // Get current year IRA
                    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    val currentIRA = iras.firstOrNull { it.taxYear == currentYear }
                    
                    // Calculate helpful metrics
                    val calculation = currentIRA?.let { ira ->
                        IRACalculation(
                            currentContributions = ira.contributionsThisYear,
                            annualLimit = ira.annualContributionLimit,
                            remainingRoom = ira.getRemainingContributionRoom(),
                            contributionsRemaining = ira.getContributionsRemainingThisYear(),
                            requiredPerContribution = ira.getRequiredContributionToMaxOut(),
                            projectedYearEnd = ira.getProjectedYearEndContributions(),
                            isOnTrack = ira.isOnTrackToMaxOut(),
                            daysRemaining = ira.getDaysRemainingInYear(),
                            recommendedBiweeklyAmount = ira.getRemainingContributionRoom() / (ira.getDaysRemainingInYear() / 14.0),
                            recommendedMonthlyAmount = ira.getRemainingContributionRoom() / (ira.getDaysRemainingInYear() / 30.0)
                        )
                    }
                    
                    _uiState.update { it.copy(
                        iras = iras,
                        currentYearIRA = currentIRA,
                        calculation = calculation,
                        isLoading = false,
                        error = null
                    )}
                }
        }
    }
    
    fun selectIRA(ira: RothIRA?) {
        _uiState.update { it.copy(selectedIRA = ira) }
    }
    
    fun addIRA(ira: RothIRA) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.addIRA(ira)) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        showAddDialog = false
                    )}
                }
                is Result.Error -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = result.message
                    )}
                }
            }
        }
    }
    
    fun updateIRA(ira: RothIRA) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.updateIRA(ira)) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        showEditDialog = false
                    )}
                }
                is Result.Error -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = result.message
                    )}
                }
            }
        }
    }
    
    fun recordContribution(iraId: String, amount: Double, taxYear: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.recordContribution(iraId, amount, taxYear)) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        showContributionDialog = false
                    )}
                }
                is Result.Error -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = result.message
                    )}
                }
            }
        }
    }
    
    fun toggleAddDialog() {
        _uiState.update { it.copy(showAddDialog = !it.showAddDialog) }
    }
    
    fun toggleEditDialog() {
        _uiState.update { it.copy(showEditDialog = !it.showEditDialog) }
    }
    
    fun toggleContributionDialog() {
        _uiState.update { it.copy(showContributionDialog = !it.showContributionDialog) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class RothIRAUiState(
    val iras: List<RothIRA> = emptyList(),
    val currentYearIRA: RothIRA? = null,
    val selectedIRA: RothIRA? = null,
    val calculation: IRACalculation? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val showContributionDialog: Boolean = false
)

