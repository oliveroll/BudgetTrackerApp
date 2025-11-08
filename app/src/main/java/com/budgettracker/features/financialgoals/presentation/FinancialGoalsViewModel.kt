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
        // Initialize from Firebase first to restore data after database wipe
        viewModelScope.launch {
            repository.initializeFromFirebase()
        }
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
        android.util.Log.d("RothIRAVM", "🔄 ViewModel initialized, loading IRAs...")
        loadIRAs()
    }
    
    fun loadIRAs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            android.util.Log.d("RothIRAVM", "⏳ Loading IRAs from repository...")
            
            // FIXED: Initialize from Firebase FIRST, then load from Room
            repository.initializeFromFirebase()
            android.util.Log.d("RothIRAVM", "✅ Firebase initialization complete, now loading from Room...")
            
            repository.getActiveIRAsFlow()
                .catch { e ->
                    android.util.Log.e("RothIRAVM", "❌ Error loading IRAs: ${e.message}", e)
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Failed to load IRAs: ${e.message}"
                    )}
                }
                .collect { iras ->
                    android.util.Log.d("RothIRAVM", "📊 Received ${iras.size} IRAs from Room")
                    iras.forEachIndexed { index, ira ->
                        android.util.Log.d("RothIRAVM", "  IRA $index: ${ira.brokerageName} | Year: ${ira.taxYear} | Contributed: ${ira.contributionsThisYear} | Active: ${ira.isActive}")
                    }
                    
                    // Get current year IRA
                    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    val currentIRA = iras.firstOrNull { it.taxYear == currentYear }
                    
                    android.util.Log.d("RothIRAVM", "Current year: $currentYear, Current IRA: ${currentIRA?.brokerageName ?: "None"}")
                    
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
                    
                    if (iras.isEmpty()) {
                        android.util.Log.w("RothIRAVM", "⚠️ No IRAs loaded - showing empty state")
                    } else {
                        android.util.Log.d("RothIRAVM", "✅ UI updated with ${iras.size} IRAs, current year IRA: ${currentIRA != null}")
                    }
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
    
    fun toggleDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = !it.showDeleteDialog) }
    }
    
    fun deleteIRA(iraId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.deleteIRA(iraId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        currentYearIRA = null,
                        showDeleteDialog = false
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
    val showContributionDialog: Boolean = false,
    val showDeleteDialog: Boolean = false
)

