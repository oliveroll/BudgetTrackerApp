package com.budgettracker.features.onboarding.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * Main onboarding flow with navigation
 */
@Composable
fun OnboardingFlow(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val onboardingData by viewModel.onboardingData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Show loading or error if needed
    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = "personal_info",
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            // Screen 1: Personal Info
            composable("personal_info") {
                PersonalInfoScreen(
                    displayName = onboardingData.displayName,
                    employmentStatus = onboardingData.employmentStatus,
                    currency = onboardingData.currency,
                    onDisplayNameChange = viewModel::updateDisplayName,
                    onEmploymentStatusChange = viewModel::updateEmploymentStatus,
                    onCurrencyChange = viewModel::updateCurrency,
                    onContinue = {
                        val validation = viewModel.validateStep1()
                        if (validation.isValid) {
                            navController.navigate("financial_goals")
                        }
                    }
                )
            }

            // Screen 2: Financial Goals
            composable("financial_goals") {
                FinancialGoalsScreen(
                    currency = onboardingData.currency,
                    monthlyBudget = onboardingData.monthlyBudget,
                    monthlySavingsGoal = onboardingData.monthlySavingsGoal,
                    primaryFinancialGoal = onboardingData.primaryFinancialGoal,
                    onMonthlyBudgetChange = viewModel::updateMonthlyBudget,
                    onMonthlySavingsGoalChange = viewModel::updateMonthlySavingsGoal,
                    onPrimaryFinancialGoalChange = viewModel::updatePrimaryFinancialGoal,
                    onContinue = {
                        val validation = viewModel.validateStep2()
                        if (validation.isValid) {
                            // Save data and navigate to success screen
                            viewModel.completeOnboarding {
                                navController.navigate("all_set") {
                                    popUpTo("personal_info") { inclusive = false }
                                }
                            }
                        }
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Screen 3: All Set (Success)
            composable("all_set") {
                AllSetScreen(
                    displayName = onboardingData.displayName,
                    onGoToDashboard = onComplete,
                    onReviewInfo = {
                        navController.navigate("personal_info") {
                            popUpTo("personal_info") { inclusive = true }
                        }
                    }
                )
            }
        }

        // Loading overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize()
            ) {
                CircularProgressIndicator()
            }
        }

        // Error snackbar
        error?.let { errorMessage ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(errorMessage)
            }
        }
    }
}

