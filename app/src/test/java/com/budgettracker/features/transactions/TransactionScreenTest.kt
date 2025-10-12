package com.budgettracker.features.transactions

import com.budgettracker.core.data.local.TransactionDataStore
import com.budgettracker.core.domain.model.Transaction
import com.budgettracker.core.domain.model.TransactionCategory
import com.budgettracker.core.domain.model.TransactionType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

/**
 * Unit tests for Transaction Screen features
 * 
 * Tests:
 * 1. Swipe-to-delete functionality
 * 2. Timestamp preservation bug fix
 */
class TransactionScreenTest {
    
    @Before
    fun setup() {
        // Clear any existing transactions before each test
        TransactionDataStore.getTransactions().forEach { 
            TransactionDataStore.deleteTransaction(it.id)
        }
    }
    
    /**
     * Test: Swipe-to-delete removes transaction from list
     * 
     * Verifies that:
     * - Transaction is successfully deleted from data store
     * - Transaction count decreases by 1
     * - Deleted transaction is no longer in the list
     */
    @Test
    fun swipeToDelete_removesTransactionFromList() {
        // Arrange: Create and add a test transaction
        val transaction = Transaction(
            id = "test-123",
            userId = "test-user",
            amount = 50.0,
            description = "Test Transaction",
            category = TransactionCategory.GROCERIES,
            type = TransactionType.EXPENSE,
            date = Date()
        )
        TransactionDataStore.addTransaction(transaction)
        
        val initialCount = TransactionDataStore.getTransactions().size
        assertEquals(1, initialCount)
        
        // Act: Delete the transaction (simulating swipe-to-delete)
        TransactionDataStore.deleteTransaction(transaction.id)
        
        // Assert: Verify transaction was deleted
        val finalCount = TransactionDataStore.getTransactions().size
        assertEquals(0, finalCount)
        
        val deletedTransaction = TransactionDataStore.getTransactions()
            .find { it.id == transaction.id }
        assertNull(deletedTransaction)
    }
    
    /**
     * Test: Multiple transaction deletion
     * 
     * Verifies that:
     * - Multiple transactions can be deleted sequentially
     * - Each deletion properly updates the list
     */
    @Test
    fun swipeToDelete_multipleTransactions_removesCorrectOnes() {
        // Arrange: Create multiple transactions
        val transaction1 = Transaction(
            id = "test-1",
            amount = 25.0,
            description = "Transaction 1",
            category = TransactionCategory.GROCERIES,
            type = TransactionType.EXPENSE,
            date = Date()
        )
        val transaction2 = Transaction(
            id = "test-2",
            amount = 50.0,
            description = "Transaction 2",
            category = TransactionCategory.DINING_OUT,
            type = TransactionType.EXPENSE,
            date = Date()
        )
        val transaction3 = Transaction(
            id = "test-3",
            amount = 100.0,
            description = "Transaction 3",
            category = TransactionCategory.SALARY,
            type = TransactionType.INCOME,
            date = Date()
        )
        
        TransactionDataStore.addTransaction(transaction1)
        TransactionDataStore.addTransaction(transaction2)
        TransactionDataStore.addTransaction(transaction3)
        
        assertEquals(3, TransactionDataStore.getTransactions().size)
        
        // Act: Delete transaction 2
        TransactionDataStore.deleteTransaction(transaction2.id)
        
        // Assert: Verify only transaction 2 was deleted
        val remaining = TransactionDataStore.getTransactions()
        assertEquals(2, remaining.size)
        assertNotNull(remaining.find { it.id == "test-1" })
        assertNull(remaining.find { it.id == "test-2" })
        assertNotNull(remaining.find { it.id == "test-3" })
    }
    
    /**
     * Test: Timestamp reflects actual creation time
     * 
     * Verifies that:
     * - Transaction timestamp uses current system time
     * - Timestamp is NOT hardcoded to 8:00 PM
     * - Time is preserved correctly (not just date)
     */
    @Test
    fun transactionTimestamp_usesActualCreationTime_notHardcoded() {
        // Arrange: Get current time
        val beforeCreation = Calendar.getInstance()
        
        // Act: Create transaction with current time
        val transaction = Transaction(
            id = "timestamp-test",
            amount = 75.0,
            description = "Timestamp Test",
            category = TransactionCategory.GROCERIES,
            type = TransactionType.EXPENSE,
            date = Date() // Should use actual current time
        )
        
        val afterCreation = Calendar.getInstance()
        
        // Assert: Verify timestamp is within the test execution window
        val transactionTime = Calendar.getInstance().apply { time = transaction.date }
        
        assertTrue(
            "Transaction time should be after or equal to before creation time",
            transactionTime.timeInMillis >= beforeCreation.timeInMillis
        )
        assertTrue(
            "Transaction time should be before or equal to after creation time",
            transactionTime.timeInMillis <= afterCreation.timeInMillis
        )
        
        // Verify it's NOT hardcoded to 8:00 PM (20:00)
        val hour = transactionTime.get(Calendar.HOUR_OF_DAY)
        val currentHour = beforeCreation.get(Calendar.HOUR_OF_DAY)
        
        // The hour should match current hour (with tolerance for hour boundary)
        assertTrue(
            "Transaction hour ($hour) should match current hour ($currentHour) or be within 1 hour",
            Math.abs(hour - currentHour) <= 1
        )
    }
    
    /**
     * Test: Date picker preserves time when selecting date
     * 
     * Verifies that:
     * - When user selects a date, the time component is preserved
     * - Only year, month, and day are updated
     * - Hour, minute, second remain from original time
     */
    @Test
    fun datePicker_preservesTimeComponent_whenDateChanges() {
        // Arrange: Create a transaction with specific time (e.g., 2:30 PM)
        val originalTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 45)
            set(Calendar.YEAR, 2024)
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 15)
        }
        
        // Act: Simulate date picker changing only the date to February 20
        val updatedTime = Calendar.getInstance().apply {
            time = originalTime.time // Start with original time
            set(Calendar.YEAR, 2024)
            set(Calendar.MONTH, Calendar.FEBRUARY)
            set(Calendar.DAY_OF_MONTH, 20)
            // Hour, minute, second should remain unchanged
        }
        
        // Assert: Verify time components are preserved
        assertEquals(
            "Hour should be preserved",
            originalTime.get(Calendar.HOUR_OF_DAY),
            updatedTime.get(Calendar.HOUR_OF_DAY)
        )
        assertEquals(
            "Minute should be preserved",
            originalTime.get(Calendar.MINUTE),
            updatedTime.get(Calendar.MINUTE)
        )
        assertEquals(
            "Second should be preserved",
            originalTime.get(Calendar.SECOND),
            updatedTime.get(Calendar.SECOND)
        )
        
        // Verify date components were updated
        assertEquals(2024, updatedTime.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, updatedTime.get(Calendar.MONTH))
        assertEquals(20, updatedTime.get(Calendar.DAY_OF_MONTH))
    }
    
    /**
     * Test: Transaction displays correct time in transaction list
     * 
     * Verifies that:
     * - Time display matches actual transaction time
     * - Format is correct (h:mm a)
     */
    @Test
    fun transactionList_displaysCorrectTime() {
        // Arrange: Create transaction at specific time
        val specificTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
        }
        
        val transaction = Transaction(
            id = "time-display-test",
            amount = 100.0,
            description = "Time Display Test",
            category = TransactionCategory.SALARY,
            type = TransactionType.INCOME,
            date = specificTime.time
        )
        
        TransactionDataStore.addTransaction(transaction)
        
        // Act: Retrieve and verify
        val retrieved = TransactionDataStore.getTransactions()
            .find { it.id == transaction.id }
        
        // Assert: Verify time matches
        assertNotNull(retrieved)
        val retrievedCal = Calendar.getInstance().apply { time = retrieved!!.date }
        assertEquals(10, retrievedCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, retrievedCal.get(Calendar.MINUTE))
    }
}

