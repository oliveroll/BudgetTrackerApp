# PostHog Usage Examples

## Integration Examples for Budget Tracker App

### 1. Authentication Screen

```kotlin
// In your login/signup screen
import com.budgettracker.core.utils.AnalyticsTracker

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    // Track when screen is viewed
    LaunchedEffect(Unit) {
        AnalyticsTracker.trackScreenViewed("Login")
    }
    
    Column {
        Button(onClick = {
            // Track login attempt
            viewModel.signInWithEmail(email, password) { result ->
                result.onSuccess { user ->
                    // Track successful login
                    AnalyticsTracker.trackUserLogin(method = "email")
                    
                    // Identify the user for session tracking
                    AnalyticsTracker.identifyUser(
                        userId = user.uid,
                        email = user.email
                    )
                    
                    onLoginSuccess()
                }.onFailure { error ->
                    // Track error
                    AnalyticsTracker.trackError(
                        errorType = "login_failed",
                        errorMessage = error.message
                    )
                }
            }
        }) {
            Text("Login with Email")
        }
        
        Button(onClick = {
            viewModel.signInWithGoogle { result ->
                result.onSuccess { user ->
                    // Track Google sign-in
                    AnalyticsTracker.trackUserLogin(method = "google")
                    AnalyticsTracker.identifyUser(user.uid, user.email)
                    onLoginSuccess()
                }
            }
        }) {
            Text("Login with Google")
        }
    }
}
```

### 2. Transaction List Screen

```kotlin
@Composable
fun TransactionsScreen(
    onAddTransaction: () -> Unit
) {
    // Track screen view
    LaunchedEffect(Unit) {
        AnalyticsTracker.trackScreenViewed("Transactions")
    }
    
    var selectedMonth by remember { mutableStateOf(LocalDate.now().monthValue) }
    var selectedYear by remember { mutableStateOf(LocalDate.now().year) }
    
    Column {
        // Month selector
        MonthSelector(
            month = selectedMonth,
            year = selectedYear,
            onMonthSelected = { month, year ->
                selectedMonth = month
                selectedYear = year
                
                // Track filter usage
                AnalyticsTracker.trackFilterApplied(
                    filterType = "date_range",
                    filterValue = "$month/$year"
                )
            }
        )
        
        // Upload PDF button
        IconButton(onClick = {
            AnalyticsTracker.trackPDFUploadStarted()
            
            // Launch PDF picker
            pdfPickerLauncher.launch("application/pdf")
        }) {
            Icon(Icons.Default.Upload, "Upload PDF")
        }
        
        // Transaction list
        LazyColumn {
            items(transactions) { transaction ->
                TransactionCard(
                    transaction = transaction,
                    onEdit = {
                        AnalyticsTracker.trackFeatureUsed("edit_transaction")
                        onEditTransaction(transaction)
                    },
                    onDelete = {
                        AnalyticsTracker.trackTransactionDeleted(
                            type = transaction.type,
                            category = transaction.category
                        )
                        viewModel.deleteTransaction(transaction.id)
                    }
                )
            }
        }
        
        // Add transaction FAB
        FloatingActionButton(onClick = {
            AnalyticsTracker.trackFeatureUsed("add_transaction")
            onAddTransaction()
        }) {
            Icon(Icons.Default.Add, "Add Transaction")
        }
    }
}
```

### 3. Add Transaction Screen

```kotlin
@Composable
fun AddTransactionScreen(
    onSave: () -> Unit
) {
    LaunchedEffect(Unit) {
        AnalyticsTracker.trackScreenViewed("AddTransaction")
    }
    
    var transactionType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf(TransactionCategory.GROCERIES) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isRecurring by remember { mutableStateOf(false) }
    var hasAttachment by remember { mutableStateOf(false) }
    
    Column {
        // Transaction type selector
        Row {
            FilterChip(
                selected = transactionType == TransactionType.INCOME,
                onClick = {
                    transactionType = TransactionType.INCOME
                    // Track type change
                    AnalyticsTracker.trackFeatureUsed("select_income_type")
                },
                label = { Text("Income") }
            )
            FilterChip(
                selected = transactionType == TransactionType.EXPENSE,
                onClick = {
                    transactionType = TransactionType.EXPENSE
                    AnalyticsTracker.trackFeatureUsed("select_expense_type")
                },
                label = { Text("Expense") }
            )
        }
        
        // Category dropdown
        CategoryDropdown(
            selectedCategory = selectedCategory,
            onCategorySelected = { category ->
                selectedCategory = category
                AnalyticsTracker.trackFeatureUsed("select_category")
            }
        )
        
        // Amount input
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") }
        )
        
        // Recurring toggle
        Row {
            Switch(
                checked = isRecurring,
                onCheckedChange = {
                    isRecurring = it
                    if (it) {
                        AnalyticsTracker.trackFeatureUsed("enable_recurring")
                    }
                }
            )
            Text("Recurring Transaction")
        }
        
        // Save button
        Button(onClick = {
            val transaction = Transaction(
                type = transactionType,
                category = selectedCategory,
                amount = amount.toDoubleOrNull() ?: 0.0,
                description = description,
                isRecurring = isRecurring
            )
            
            viewModel.addTransaction(transaction) { result ->
                result.onSuccess {
                    // Track successful addition
                    AnalyticsTracker.trackTransactionAdded(
                        type = transactionType,
                        category = selectedCategory,
                        isRecurring = isRecurring,
                        hasAttachment = hasAttachment
                    )
                    onSave()
                }.onFailure { error ->
                    // Track error
                    AnalyticsTracker.trackError(
                        errorType = "transaction_add_failed",
                        errorMessage = error.message
                    )
                }
            }
        }) {
            Text("Save Transaction")
        }
    }
}
```

### 4. PDF Upload Handler

```kotlin
// In your ViewModel or Repository
fun uploadPDFStatement(uri: Uri) {
    viewModelScope.launch {
        try {
            // Track start
            AnalyticsTracker.trackPDFUploadStarted()
            
            val transactions = pdfParser.parseRegionsBankStatement(uri)
            
            // Save transactions
            transactions.forEach { transaction ->
                transactionRepository.addTransaction(transaction)
            }
            
            // Track success
            AnalyticsTracker.trackPDFUploadSuccess(
                transactionCount = transactions.size
            )
            
            // Also track bulk import
            AnalyticsTracker.trackBulkTransactionsImported(
                count = transactions.size,
                source = "regions_bank_pdf"
            )
            
            _uploadState.value = UploadState.Success(transactions.size)
            
        } catch (e: Exception) {
            // Track failure
            AnalyticsTracker.trackPDFUploadFailed(
                errorReason = e.message ?: "Unknown error"
            )
            
            _uploadState.value = UploadState.Error(e.message)
        }
    }
}
```

### 5. Dashboard Screen

```kotlin
@Composable
fun DashboardScreen() {
    LaunchedEffect(Unit) {
        AnalyticsTracker.trackDashboardViewed()
        AnalyticsTracker.trackScreenViewed("Dashboard")
    }
    
    Column {
        // Financial overview card
        FinancialOverviewCard(
            income = totalIncome,
            expenses = totalExpenses,
            onCardClick = {
                AnalyticsTracker.trackFeatureUsed("financial_overview_card")
            }
        )
        
        // Spending by category chart
        SpendingByCategoryChart(
            data = categoryData,
            onChartClick = {
                AnalyticsTracker.trackChartInteraction("spending_by_category")
            }
        )
        
        // Income vs Expenses chart
        IncomeVsExpensesChart(
            data = monthlyData,
            onChartClick = {
                AnalyticsTracker.trackChartInteraction("income_vs_expenses")
            }
        )
        
        // Budget alerts
        if (isBudgetExceeded) {
            BudgetAlert(
                category = exceededCategory,
                onClick = {
                    AnalyticsTracker.trackBudgetAlertViewed(
                        categoryOverBudget = true
                    )
                    navController.navigate("budget")
                }
            )
        }
    }
}
```

### 6. Budget Screen

```kotlin
@Composable
fun BudgetScreen() {
    LaunchedEffect(Unit) {
        AnalyticsTracker.trackScreenViewed("Budget")
    }
    
    Column {
        // Create budget button
        Button(onClick = {
            AnalyticsTracker.trackFeatureUsed("create_budget")
            showBudgetDialog = true
        }) {
            Text("Create Budget")
        }
        
        // Budget templates
        Row {
            BudgetTemplateCard(
                name = "50/30/20 Rule",
                onClick = {
                    AnalyticsTracker.trackBudgetCreated(templateUsed = "50_30_20")
                    viewModel.createBudget(Budget50_30_20Template)
                }
            )
            
            BudgetTemplateCard(
                name = "Zero-Based",
                onClick = {
                    AnalyticsTracker.trackBudgetCreated(templateUsed = "zero_based")
                    viewModel.createBudget(ZeroBasedTemplate)
                }
            )
        }
        
        // Edit budget button
        IconButton(onClick = {
            AnalyticsTracker.trackFeatureUsed("edit_budget")
            AnalyticsTracker.trackBudgetEdited()
            showEditDialog = true
        }) {
            Icon(Icons.Default.Edit, "Edit Budget")
        }
    }
}
```

### 7. Savings Goals Screen

```kotlin
@Composable
fun SavingsGoalsScreen() {
    LaunchedEffect(Unit) {
        AnalyticsTracker.trackScreenViewed("SavingsGoals")
    }
    
    Column {
        // Add goal button
        Button(onClick = {
            AnalyticsTracker.trackFeatureUsed("create_savings_goal")
            showCreateGoalDialog = true
        }) {
            Text("Create Savings Goal")
        }
        
        // Goals list
        LazyColumn {
            items(savingsGoals) { goal ->
                SavingsGoalCard(
                    goal = goal,
                    onProgressUpdate = { newProgress ->
                        val percentComplete = (newProgress / goal.targetAmount * 100).toInt()
                        
                        AnalyticsTracker.trackSavingsGoalProgressUpdated(
                            percentComplete = percentComplete
                        )
                        
                        if (percentComplete >= 100) {
                            AnalyticsTracker.trackSavingsGoalCompleted(
                                goalType = goal.type
                            )
                        }
                        
                        viewModel.updateGoalProgress(goal.id, newProgress)
                    }
                )
            }
        }
    }
}

// In the create goal dialog
fun createSavingsGoal(goalType: String, targetAmount: Double) {
    AnalyticsTracker.trackSavingsGoalCreated(goalType = goalType)
    
    viewModel.createGoal(
        SavingsGoal(
            type = goalType,
            targetAmount = targetAmount,
            currentAmount = 0.0
        )
    )
}
```

### 8. Settings Screen

```kotlin
@Composable
fun SettingsScreen() {
    LaunchedEffect(Unit) {
        AnalyticsTracker.trackScreenViewed("Settings")
    }
    
    Column {
        // Currency setting
        DropdownSetting(
            label = "Currency",
            value = currentCurrency,
            options = listOf("USD", "EUR", "GBP"),
            onValueChange = { newCurrency ->
                AnalyticsTracker.trackSettingChanged(
                    settingName = "currency",
                    newValue = newCurrency
                )
                viewModel.updateCurrency(newCurrency)
            }
        )
        
        // Notification setting
        SwitchSetting(
            label = "Enable Notifications",
            checked = notificationsEnabled,
            onCheckedChange = { enabled ->
                AnalyticsTracker.trackNotificationPreferenceChanged(enabled)
                viewModel.updateNotifications(enabled)
            }
        )
        
        // Export data
        Button(onClick = {
            AnalyticsTracker.trackFeatureUsed("export_data")
            viewModel.exportData()
        }) {
            Text("Export Data")
        }
        
        // Logout
        Button(onClick = {
            // Important: Track and reset session before logout
            AnalyticsTracker.flush() // Ensure all events are sent
            AnalyticsTracker.trackUserLogout() // Also calls PostHog.reset()
            
            viewModel.logout()
        }) {
            Text("Logout")
        }
    }
}
```

### 9. Subscription Management

```kotlin
@Composable
fun SubscriptionCard(subscription: Transaction) {
    Card {
        Column {
            Text("${subscription.category.displayName} - $${subscription.amount}")
            
            Switch(
                checked = subscription.isActive,
                onCheckedChange = { isActive ->
                    if (!isActive) {
                        // Track cancellation
                        AnalyticsTracker.trackSubscriptionCancelled(
                            category = subscription.category.displayName
                        )
                    }
                    viewModel.updateSubscription(subscription.copy(isActive = isActive))
                }
            )
        }
    }
}

// When adding a new subscription
fun addSubscription(subscription: Transaction) {
    AnalyticsTracker.trackSubscriptionAdded(
        category = subscription.category.displayName
    )
    
    AnalyticsTracker.trackTransactionAdded(
        type = subscription.type,
        category = subscription.category,
        isRecurring = true,
        hasAttachment = false
    )
    
    viewModel.addTransaction(subscription)
}
```

### 10. Error Handling

```kotlin
// In your Repository or ViewModel
suspend fun syncWithFirebase() {
    try {
        val result = firebaseRepository.syncTransactions()
        
        result.onFailure { error ->
            // Track sync errors
            AnalyticsTracker.trackError(
                errorType = "firebase_sync_failed",
                errorMessage = error.message
            )
            
            if (error is NetworkException) {
                AnalyticsTracker.trackNetworkError("/api/sync")
            }
        }
    } catch (e: Exception) {
        AnalyticsTracker.trackError(
            errorType = "unexpected_error",
            errorMessage = e.stackTraceToString()
        )
    }
}
```

## Best Practices Recap

1. **Track screen views** at the top of each Composable with `LaunchedEffect(Unit)`
2. **Track user actions** when buttons are clicked or features are used
3. **Track outcomes** - success and failure of operations
4. **Never track sensitive data** - use the `AnalyticsTracker` helper methods
5. **Flush before logout** - ensure events are sent before user session ends
6. **Identify users** after successful authentication
7. **Reset on logout** - clear PostHog session data

## Privacy Note

All tracking methods in `AnalyticsTracker` are designed to:
- ✅ Track user behavior and feature usage
- ✅ Provide insights into app performance
- ❌ Never send exact transaction amounts
- ❌ Never send personal financial details
- ❌ Never send account or card numbers

If you need to track anything with amounts, the amounts will be automatically sanitized to ranges by the `propertiesSanitizer` in the PostHog configuration.




