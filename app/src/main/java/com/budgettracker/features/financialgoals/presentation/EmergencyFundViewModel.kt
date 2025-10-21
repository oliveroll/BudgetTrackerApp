package com.budgettracker.features.financialgoals.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgettracker.core.data.repository.EmergencyFundRepository
import com.budgettracker.core.domain.model.EmergencyFund
import com.budgettracker.core.domain.model.EmergencyFundProjection
import com.budgettracker.core.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Emergency Fund tab
 */
@HiltViewModel
class EmergencyFundViewModel @Inject constructor(
    private val repository: EmergencyFundRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EmergencyFundUiState())
    val uiState: StateFlow<EmergencyFundUiState> = _uiState.asStateFlow()
    
    init {
        loadFunds()
    }
    
    fun loadFunds() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            repository.getActiveFundsFlow()
                .catch { e ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Failed to load emergency funds: ${e.message}"
                    )}
                }
                .collect { funds ->
                    // Select first fund if available
                    val selectedFund = funds.firstOrNull()
                    
                    // Generate projections for selected fund
                    val projections = selectedFund?.generateProjectionSchedule(12) ?: emptyList()
                    
                    _uiState.update { it.copy(
                        funds = funds,
                        selectedFund = selectedFund,
                        projections = projections,
                        isLoading = false,
                        error = null
                    )}
                }
        }
    }
    
    fun selectFund(fund: EmergencyFund?) {
        val projections = fund?.generateProjectionSchedule(12) ?: emptyList()
        _uiState.update { it.copy(
            selectedFund = fund,
            projections = projections
        )}
    }
    
    fun addFund(fund: EmergencyFund) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.addFund(fund)) {
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
    
    fun updateFund(fund: EmergencyFund) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.updateFund(fund)) {
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
    
    fun deleteFund(fundId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.deleteFund(fundId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        selectedFund = null
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
    
    fun recordDeposit(fundId: String, amount: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.recordDeposit(fundId, amount, false)) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        showDepositDialog = false
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
    
    fun updateProjectionMonths(months: Int) {
        val projections = _uiState.value.selectedFund?.generateProjectionSchedule(months) ?: emptyList()
        _uiState.update { it.copy(
            projections = projections,
            projectionMonths = months
        )}
    }
    
    fun toggleAddDialog() {
        _uiState.update { it.copy(showAddDialog = !it.showAddDialog) }
    }
    
    fun toggleEditDialog() {
        _uiState.update { it.copy(showEditDialog = !it.showEditDialog) }
    }
    
    fun toggleDepositDialog() {
        _uiState.update { it.copy(showDepositDialog = !it.showDepositDialog) }
    }
    
    fun toggleDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = !it.showDeleteDialog) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class EmergencyFundUiState(
    val funds: List<EmergencyFund> = emptyList(),
    val selectedFund: EmergencyFund? = null,
    val projections: List<EmergencyFundProjection> = emptyList(),
    val projectionMonths: Int = 12,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val showDepositDialog: Boolean = false,
    val showDeleteDialog: Boolean = false
)

