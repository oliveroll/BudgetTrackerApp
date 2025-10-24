package com.budgettracker.features.financialgoals.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgettracker.core.data.repository.ETFPortfolioRepository
import com.budgettracker.core.domain.model.*
import com.budgettracker.core.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for ETF Portfolio tab
 */
@HiltViewModel
class ETFPortfolioViewModel @Inject constructor(
    private val repository: ETFPortfolioRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ETFPortfolioUiState())
    val uiState: StateFlow<ETFPortfolioUiState> = _uiState.asStateFlow()
    
    init {
        // Initialize from Firebase first to restore data after database wipe
        viewModelScope.launch {
            repository.initializeFromFirebase()
        }
        loadPortfolios()
    }
    
    fun loadPortfolios() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            repository.getActivePortfoliosFlow()
                .catch { e ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Failed to load portfolios: ${e.message}"
                    )}
                }
                .collect { portfolios ->
                    val selectedPortfolio = portfolios.firstOrNull()
                    
                    // Load holdings for selected portfolio
                    if (selectedPortfolio != null) {
                        loadHoldings(selectedPortfolio.id)
                    }
                    
                    _uiState.update { it.copy(
                        portfolios = portfolios,
                        selectedPortfolio = selectedPortfolio,
                        isLoading = false,
                        error = null
                    )}
                }
        }
    }
    
    fun selectPortfolio(portfolio: ETFPortfolio?) {
        _uiState.update { it.copy(selectedPortfolio = portfolio) }
        if (portfolio != null) {
            loadHoldings(portfolio.id)
        }
    }
    
    fun loadHoldings(portfolioId: String) {
        viewModelScope.launch {
            repository.getHoldingsFlow(portfolioId)
                .catch { e ->
                    _uiState.update { it.copy(error = "Failed to load holdings: ${e.message}") }
                }
                .collect { holdings ->
                    // Calculate portfolio metrics
                    val performance = calculatePortfolioPerformance(holdings)
                    
                    _uiState.update { it.copy(
                        holdings = holdings,
                        performance = performance
                    )}
                }
        }
    }
    
    fun addPortfolio(portfolio: ETFPortfolio) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.addPortfolio(portfolio)) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        showAddPortfolioDialog = false
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
    
    fun updatePortfolio(portfolio: ETFPortfolio) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.updatePortfolio(portfolio)) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        showEditPortfolioDialog = false
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
    
    fun deletePortfolio(portfolioId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.deletePortfolio(portfolioId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        selectedPortfolio = null
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
    
    fun addHolding(holding: ETFHolding) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.addHolding(holding)) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        showAddHoldingDialog = false
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
    
    fun updateHolding(holding: ETFHolding) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.updateHolding(holding)) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        showEditHoldingDialog = false,
                        selectedHolding = null
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
    
    fun deleteHolding(holdingId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.deleteHolding(holdingId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        selectedHolding = null
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
    
    fun recordTransaction(
        portfolioId: String,
        holdingId: String,
        ticker: String,
        transactionType: InvestmentTransaction.TransactionType,
        shares: Double,
        pricePerShare: Double,
        fees: Double = 0.0
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.recordTransaction(
                portfolioId, holdingId, ticker, transactionType, shares, pricePerShare, fees
            )) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        showTransactionDialog = false
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
    
    fun selectHolding(holding: ETFHolding?) {
        _uiState.update { it.copy(selectedHolding = holding) }
    }
    
    fun toggleAddPortfolioDialog() {
        _uiState.update { it.copy(showAddPortfolioDialog = !it.showAddPortfolioDialog) }
    }
    
    fun toggleEditPortfolioDialog() {
        _uiState.update { it.copy(showEditPortfolioDialog = !it.showEditPortfolioDialog) }
    }
    
    fun toggleAddHoldingDialog() {
        _uiState.update { it.copy(showAddHoldingDialog = !it.showAddHoldingDialog) }
    }
    
    fun toggleEditHoldingDialog() {
        _uiState.update { it.copy(showEditHoldingDialog = !it.showEditHoldingDialog) }
    }
    
    fun toggleTransactionDialog() {
        _uiState.update { it.copy(showTransactionDialog = !it.showTransactionDialog) }
    }
    
    fun toggleDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = !it.showDeleteDialog) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    private fun calculatePortfolioPerformance(holdings: List<ETFHolding>): PortfolioPerformance {
        val totalValue = holdings.sumOf { it.getCurrentValue() }
        val totalCostBasis = holdings.sumOf { it.costBasis }
        val totalGainLoss = totalValue - totalCostBasis
        val gainLossPercentage = if (totalCostBasis > 0) {
            (totalGainLoss / totalCostBasis * 100).toFloat()
        } else 0f
        
        val annualDividendIncome = holdings.sumOf { it.getAnnualDividendIncome() }
        val annualExpenseRatio = holdings.sumOf { 
            it.getCurrentValue() * (it.expenseRatio / 100)
        }
        
        // Calculate diversification score (simplified)
        val assetClassCount = holdings.map { it.assetClass }.distinct().count()
        val diversificationScore = (assetClassCount / AssetClass.values().size.toFloat() * 100).coerceAtMost(100f)
        
        return PortfolioPerformance(
            totalValue = totalValue,
            totalCostBasis = totalCostBasis,
            totalGainLoss = totalGainLoss,
            gainLossPercentage = gainLossPercentage,
            annualDividendIncome = annualDividendIncome,
            annualExpenseRatio = annualExpenseRatio,
            diversificationScore = diversificationScore,
            needsRebalancing = false, // Would need portfolio target allocation
            allocationDeviations = emptyMap()
        )
    }
}

data class ETFPortfolioUiState(
    val portfolios: List<ETFPortfolio> = emptyList(),
    val selectedPortfolio: ETFPortfolio? = null,
    val holdings: List<ETFHolding> = emptyList(),
    val selectedHolding: ETFHolding? = null,
    val performance: PortfolioPerformance? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddPortfolioDialog: Boolean = false,
    val showEditPortfolioDialog: Boolean = false,
    val showAddHoldingDialog: Boolean = false,
    val showEditHoldingDialog: Boolean = false,
    val showTransactionDialog: Boolean = false,
    val showDeleteDialog: Boolean = false
)

