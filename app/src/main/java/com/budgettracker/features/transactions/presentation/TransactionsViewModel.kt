package com.budgettracker.features.transactions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgettracker.core.domain.model.Transaction
import com.budgettracker.core.domain.model.TransactionCategory
import com.budgettracker.core.domain.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * ViewModel for transactions that persists data across navigation
 */
@HiltViewModel
class TransactionsViewModel @Inject constructor() : ViewModel() {
    
    private val _transactions = MutableStateFlow(getSampleTransactionsDetailed())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()
    
    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()
    
    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    /**
     * Add parsed transactions from PDF
     */
    fun addParsedTransactions(newTransactions: List<Transaction>) {
        viewModelScope.launch {
            val currentTransactions = _transactions.value.toMutableList()
            currentTransactions.addAll(newTransactions)
            _transactions.value = currentTransactions
            
            // TODO: Save to Firebase
            saveTransactionsToFirebase(newTransactions)
        }
    }
    
    /**
     * Update existing transaction
     */
    fun updateTransaction(updatedTransaction: Transaction) {
        viewModelScope.launch {
            val currentTransactions = _transactions.value.toMutableList()
            val index = currentTransactions.indexOfFirst { it.id == updatedTransaction.id }
            if (index != -1) {
                currentTransactions[index] = updatedTransaction
                _transactions.value = currentTransactions
                
                // TODO: Update in Firebase
                saveTransactionToFirebase(updatedTransaction)
            }
        }
    }
    
    /**
     * Delete transaction
     */
    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            val currentTransactions = _transactions.value.toMutableList()
            currentTransactions.removeAll { it.id == transactionId }
            _transactions.value = currentTransactions
            
            // TODO: Delete from Firebase
            deleteTransactionFromFirebase(transactionId)
        }
    }
    
    /**
     * Set selected month and year
     */
    fun setSelectedMonth(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value = year
    }
    
    /**
     * Get transactions for specific month/year
     */
    fun getTransactionsForMonth(month: Int, year: Int): List<Transaction> {
        return _transactions.value.filter { transaction ->
            // FIXED: Use LocalDate methods instead of Calendar
            transaction.date.monthValue == (month + 1) && transaction.date.year == year
        }
    }
    
    /**
     * Save transactions to Firebase (placeholder)
     */
    private suspend fun saveTransactionsToFirebase(transactions: List<Transaction>) {
        _isLoading.value = true
        try {
            // TODO: Implement Firebase Firestore save
            // For now, just simulate a network call
            kotlinx.coroutines.delay(1000)
            
            // In real implementation:
            // firestore.collection("transactions")
            //     .add(transaction)
            //     .await()
            
        } catch (e: Exception) {
            // Handle error
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Save single transaction to Firebase (placeholder)
     */
    private suspend fun saveTransactionToFirebase(transaction: Transaction) {
        _isLoading.value = true
        try {
            // TODO: Implement Firebase Firestore update
            kotlinx.coroutines.delay(500)
            
        } catch (e: Exception) {
            // Handle error
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Delete transaction from Firebase (placeholder)
     */
    private suspend fun deleteTransactionFromFirebase(transactionId: String) {
        _isLoading.value = true
        try {
            // TODO: Implement Firebase Firestore delete
            kotlinx.coroutines.delay(500)
            
        } catch (e: Exception) {
            // Handle error
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Sample transactions data (same as before but now persistent)
     */
    private fun getSampleTransactionsDetailed(): List<Transaction> {
        return listOf(
            Transaction(
                id = "1",
                userId = "demo_user",
                amount = 2523.88,
                category = TransactionCategory.SALARY,
                type = TransactionType.INCOME,
                description = "Salary Deposit - Ixana Quasistatics",
                date = java.time.LocalDate.now(),
                notes = "Bi-weekly salary deposit"
            ),
            Transaction(
                id = "2",
                userId = "demo_user",
                amount = 475.0,
                category = TransactionCategory.LOAN_PAYMENT,
                type = TransactionType.EXPENSE,
                description = "German Student Loan Payment",
                date = java.time.LocalDate.now().minusDays(1),
                notes = "€450 monthly payment"
            ),
            Transaction(
                id = "3",
                userId = "demo_user",
                amount = 75.50,
                category = TransactionCategory.GROCERIES,
                type = TransactionType.EXPENSE,
                description = "Grocery Shopping - Walmart",
                date = java.time.LocalDate.now(),
                notes = "Weekly groceries"
            ),
            Transaction(
                id = "4",
                userId = "demo_user",
                amount = 1200.0,
                category = TransactionCategory.RENT,
                type = TransactionType.EXPENSE,
                description = "Monthly Rent Payment",
                date = java.time.LocalDate.now().minusDays(2),
                notes = "Apartment rent"
            ),
            Transaction(
                id = "5",
                userId = "demo_user",
                amount = 45.0,
                category = TransactionCategory.PHONE,
                type = TransactionType.EXPENSE,
                description = "Phone Plan - Verizon",
                date = java.time.LocalDate.now().minusDays(3),
                notes = "Monthly phone bill"
            ),
            // Add some transactions from previous months for testing
            Transaction(
                id = "6",
                userId = "demo_user",
                amount = 2523.88,
                category = TransactionCategory.SALARY,
                type = TransactionType.INCOME,
                description = "Salary Deposit - August",
                date = java.time.LocalDate.now().minusDays(30), // Last month
                notes = "Previous month salary"
            ),
            Transaction(
                id = "7",
                userId = "demo_user",
                amount = 150.0,
                category = TransactionCategory.INSURANCE,
                type = TransactionType.EXPENSE,
                description = "Car Insurance - August",
                date = java.time.LocalDate.now().minusDays(32), // Last month
                notes = "Monthly car insurance"
            )
        )
    }
}
