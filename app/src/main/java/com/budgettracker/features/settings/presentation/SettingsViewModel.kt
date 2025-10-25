package com.budgettracker.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgettracker.core.utils.Result
import com.budgettracker.features.settings.data.models.*
import com.budgettracker.features.settings.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {
    
    // State flows
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    // Data flows from repository
    val userSettings: StateFlow<UserSettings?> = repository.getUserSettingsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    val employment: StateFlow<EmploymentSettings?> = repository.getActiveEmploymentFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    val categories: StateFlow<List<CustomCategory>> = repository.getActiveCategoriesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    init {
        initializeSettings()
    }
    
    private fun initializeSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (repository.initializeUserSettings()) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, error = null) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Failed to load settings"
                    )}
                }
            }
        }
    }
    
    // ============================================
    // UI STATE MANAGEMENT
    // ============================================
    
    fun showEmailChangeDialog() {
        _uiState.update { it.copy(showEmailChangeDialog = true) }
    }
    
    fun hideEmailChangeDialog() {
        _uiState.update { it.copy(showEmailChangeDialog = false) }
    }
    
    fun showEmploymentDialog() {
        _uiState.update { it.copy(showEmploymentDialog = true) }
    }
    
    fun hideEmploymentDialog() {
        _uiState.update { it.copy(showEmploymentDialog = false) }
    }
    
    fun showCategoryDialog(category: CustomCategory? = null) {
        _uiState.update { it.copy(
            showCategoryDialog = true,
            editingCategory = category
        )}
    }
    
    fun hideCategoryDialog() {
        _uiState.update { it.copy(
            showCategoryDialog = false,
            editingCategory = null
        )}
    }
    
    fun showDeleteAccountDialog() {
        _uiState.update { it.copy(showDeleteAccountDialog = true) }
    }
    
    fun hideDeleteAccountDialog() {
        _uiState.update { it.copy(showDeleteAccountDialog = false) }
    }
    
    fun setMessage(message: String) {
        _uiState.update { it.copy(message = message) }
    }
    
    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
    
    fun setError(error: String) {
        _uiState.update { it.copy(error = error) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    // ============================================
    // SETTINGS ACTIONS
    // ============================================
    
    fun updateNotificationSettings(settings: NotificationSettings) {
        viewModelScope.launch {
            val currentSettings = userSettings.value ?: return@launch
            
            when (repository.updateUserSettings(currentSettings.copy(
                notificationSettings = settings,
                updatedAt = System.currentTimeMillis()
            ))) {
                is Result.Success -> {
                    setMessage("Notification settings updated")
                }
                is Result.Error -> {
                    setError("Failed to update notification settings")
                }
            }
        }
    }
    
    fun updateCurrency(currency: String, symbol: String) {
        viewModelScope.launch {
            val currentSettings = userSettings.value ?: return@launch
            
            when (repository.updateUserSettings(currentSettings.copy(
                currency = currency,
                currencySymbol = symbol,
                updatedAt = System.currentTimeMillis()
            ))) {
                is Result.Success -> {
                    setMessage("Currency updated to $currency")
                }
                is Result.Error -> {
                    setError("Failed to update currency")
                }
            }
        }
    }
    
    fun changeEmail(currentPassword: String, newEmail: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = repository.changeEmail(currentPassword, newEmail)) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        showEmailChangeDialog = false
                    )}
                    setMessage("Email updated successfully")
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
    
    fun saveEmployment(employment: EmploymentSettings) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (repository.saveEmploymentSettings(employment)) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        showEmploymentDialog = false
                    )}
                    setMessage("Employment details saved")
                }
                is Result.Error -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Failed to save employment details"
                    )}
                }
            }
        }
    }
    
    fun addCategory(category: CustomCategory) {
        viewModelScope.launch {
            when (repository.addCategory(category)) {
                is Result.Success -> {
                    hideCategoryDialog()
                    setMessage("Category added")
                }
                is Result.Error -> {
                    setError("Failed to add category")
                }
            }
        }
    }
    
    fun updateCategory(category: CustomCategory) {
        viewModelScope.launch {
            when (repository.updateCategory(category)) {
                is Result.Success -> {
                    hideCategoryDialog()
                    setMessage("Category updated")
                }
                is Result.Error -> {
                    setError("Failed to update category")
                }
            }
        }
    }
    
    fun archiveCategory(categoryId: String) {
        viewModelScope.launch {
            when (repository.archiveCategory(categoryId)) {
                is Result.Success -> {
                    setMessage("Category archived")
                }
                is Result.Error -> {
                    setError("Failed to archive category")
                }
            }
        }
    }
    
    fun exportData(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = repository.exportUserData()) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess(result.data)
                    setMessage("Data exported successfully")
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
    
    fun deleteAccount(password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = repository.deleteAccount(password)) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        showDeleteAccountDialog = false
                    )}
                    onSuccess()
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
}

/**
 * UI State for Settings Screen
 */
data class SettingsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    
    // Dialog states
    val showEmailChangeDialog: Boolean = false,
    val showEmploymentDialog: Boolean = false,
    val showCategoryDialog: Boolean = false,
    val showDeleteAccountDialog: Boolean = false,
    
    // Editing state
    val editingCategory: CustomCategory? = null
)

