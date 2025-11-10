package com.budgettracker.features.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgettracker.features.onboarding.domain.models.*
import com.budgettracker.features.settings.data.repository.SettingsRepository
import com.budgettracker.features.settings.data.models.UserSettings
import com.budgettracker.features.settings.data.models.EmploymentSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for onboarding flow
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _onboardingData = MutableStateFlow(OnboardingData())
    val onboardingData: StateFlow<OnboardingData> = _onboardingData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    /**
     * Update display name
     */
    fun updateDisplayName(name: String) {
        _onboardingData.value = _onboardingData.value.copy(displayName = name)
    }

    /**
     * Update employment status
     */
    fun updateEmploymentStatus(status: EmploymentStatus) {
        _onboardingData.value = _onboardingData.value.copy(employmentStatus = status)
    }

    /**
     * Update currency
     */
    fun updateCurrency(currency: Currency) {
        _onboardingData.value = _onboardingData.value.copy(currency = currency)
    }

    /**
     * Update monthly budget
     */
    fun updateMonthlyBudget(budget: Double?) {
        _onboardingData.value = _onboardingData.value.copy(monthlyBudget = budget)
    }

    /**
     * Update monthly savings goal
     */
    fun updateMonthlySavingsGoal(goal: Double?) {
        _onboardingData.value = _onboardingData.value.copy(monthlySavingsGoal = goal)
    }

    /**
     * Update primary financial goal
     */
    fun updatePrimaryFinancialGoal(goal: FinancialGoal?) {
        _onboardingData.value = _onboardingData.value.copy(primaryFinancialGoal = goal)
    }

    /**
     * Validate step 1 (Personal Info)
     */
    fun validateStep1(): ValidationResult {
        val data = _onboardingData.value
        val errors = mutableMapOf<String, String>()

        if (data.displayName.isBlank()) {
            errors["displayName"] = "Display name is required"
        } else if (data.displayName.length > 30) {
            errors["displayName"] = "Display name must be 30 characters or less"
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    /**
     * Validate step 2 (Financial Goals)
     */
    fun validateStep2(): ValidationResult {
        val data = _onboardingData.value
        val errors = mutableMapOf<String, String>()

        if (data.monthlyBudget != null && data.monthlyBudget <= 0) {
            errors["monthlyBudget"] = "Monthly budget must be greater than 0"
        }

        if (data.monthlySavingsGoal != null && data.monthlySavingsGoal <= 0) {
            errors["monthlySavingsGoal"] = "Savings goal must be greater than 0"
        }

        if (data.monthlyBudget != null && data.monthlySavingsGoal != null) {
            if (data.monthlySavingsGoal > data.monthlyBudget) {
                errors["monthlySavingsGoal"] = "Savings goal cannot exceed monthly budget"
            }
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    /**
     * Go to next step
     */
    fun nextStep() {
        _currentStep.value += 1
    }

    /**
     * Go to previous step
     */
    fun previousStep() {
        _currentStep.value -= 1
    }

    /**
     * Complete onboarding and save data
     */
    fun completeOnboarding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val data = _onboardingData.value

                // Initialize user settings if needed
                settingsRepository.initializeUserSettings()

                // Get current settings (use firstOrNull to get single emission)
                val currentSettings = settingsRepository.getUserSettingsFlow().firstOrNull()
                if (currentSettings != null) {
                    // Update user settings with onboarding data
                    val updatedSettings = currentSettings.copy(
                        displayName = data.displayName,
                        currency = data.currency.code,
                        currencySymbol = data.currency.symbol
                    )

                    settingsRepository.updateUserSettings(updatedSettings)
                }

                // Get or create employment settings (use firstOrNull to get single emission)
                val currentEmployment = settingsRepository.getActiveEmploymentFlow().firstOrNull()
                val employmentToSave = if (currentEmployment != null) {
                    currentEmployment.copy(
                        optStatus = when (data.employmentStatus) {
                            EmploymentStatus.EMPLOYED_OPT -> "OPT"
                            EmploymentStatus.STUDENT -> "Student"
                            EmploymentStatus.OPT_SEEKING -> "OPT (Seeking Employment)"
                            EmploymentStatus.SELF_EMPLOYED -> "Self-Employed"
                            EmploymentStatus.OTHER -> "Other"
                        },
                        employmentType = when (data.employmentStatus) {
                            EmploymentStatus.EMPLOYED_OPT, EmploymentStatus.SELF_EMPLOYED -> "FULL_TIME"
                            EmploymentStatus.STUDENT -> "STUDENT"
                            else -> "OTHER"
                        }
                    )
                } else {
                    // Create new employment settings
                    val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    EmploymentSettings(
                        id = java.util.UUID.randomUUID().toString(),
                        userId = userId,
                        optStatus = when (data.employmentStatus) {
                            EmploymentStatus.EMPLOYED_OPT -> "OPT"
                            EmploymentStatus.STUDENT -> "Student"
                            EmploymentStatus.OPT_SEEKING -> "OPT (Seeking Employment)"
                            EmploymentStatus.SELF_EMPLOYED -> "Self-Employed"
                            EmploymentStatus.OTHER -> "Other"
                        },
                        employmentType = when (data.employmentStatus) {
                            EmploymentStatus.EMPLOYED_OPT, EmploymentStatus.SELF_EMPLOYED -> "FULL_TIME"
                            EmploymentStatus.STUDENT -> "STUDENT"
                            else -> "OTHER"
                        }
                    )
                }

                settingsRepository.saveEmploymentSettings(employmentToSave)

                // FIXED: Mark onboarding as completed
                settingsRepository.markOnboardingComplete()
                android.util.Log.d("OnboardingViewModel", "✅ Onboarding completed and marked")

                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message ?: "Failed to save onboarding data"
            }
        }
    }

    /**
     * Clear error
     */
    fun clearError() {
        _error.value = null
    }
}

