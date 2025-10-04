package com.budgettracker.core.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.budgettracker.core.data.local.database.BudgetTrackerDatabase
import com.budgettracker.core.data.local.entities.LoanEntity
import com.budgettracker.core.domain.model.LoanType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class LoanDaoTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private lateinit var database: BudgetTrackerDatabase
    private lateinit var loanDao: LoanDao
    
    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BudgetTrackerDatabase::class.java
        ).allowMainThreadQueries().build()
        
        loanDao = database.loanDao()
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    @Test
    fun insertAndRetrieveLoan() = runTest {
        val loan = LoanEntity(
            id = "test-loan-1",
            userId = "user-1",
            name = "German Student Loan",
            originalAmount = 10439.01,
            remainingAmount = 10317.64,
            interestRate = 6.66,
            monthlyPayment = 900.0,
            currency = "EUR",
            startDate = Date().time,
            estimatedPayoffDate = Date().time,
            loanType = LoanType.STUDENT_LOAN,
            lender = "German Government",
            isActive = true,
            createdAt = Date().time,
            updatedAt = Date().time,
            notes = "Test loan"
        )
        
        loanDao.insertLoan(loan)
        
        val retrieved = loanDao.getLoanById("test-loan-1")
        assertNotNull(retrieved)
        assertEquals(loan.name, retrieved?.name)
        assertEquals(loan.remainingAmount, retrieved?.remainingAmount, 0.01)
    }
    
    @Test
    fun getLoansByUserId() = runTest {
        val loan1 = LoanEntity(
            id = "loan-1",
            userId = "user-1",
            name = "Loan 1",
            originalAmount = 1000.0,
            remainingAmount = 800.0,
            interestRate = 5.0,
            monthlyPayment = 100.0,
            currency = "EUR",
            startDate = Date().time,
            estimatedPayoffDate = Date().time,
            loanType = LoanType.STUDENT_LOAN,
            lender = "Bank",
            isActive = true,
            createdAt = Date().time,
            updatedAt = Date().time,
            notes = ""
        )
        
        val loan2 = LoanEntity(
            id = "loan-2",
            userId = "user-1",
            name = "Loan 2",
            originalAmount = 2000.0,
            remainingAmount = 1500.0,
            interestRate = 6.0,
            monthlyPayment = 200.0,
            currency = "EUR",
            startDate = Date().time,
            estimatedPayoffDate = Date().time,
            loanType = LoanType.PERSONAL_LOAN,
            lender = "Bank",
            isActive = true,
            createdAt = Date().time,
            updatedAt = Date().time,
            notes = ""
        )
        
        loanDao.insertLoans(listOf(loan1, loan2))
        
        val loans = loanDao.getLoansByUserId("user-1").first()
        assertEquals(2, loans.size)
    }
    
    @Test
    fun updateRemainingAmount() = runTest {
        val loan = LoanEntity(
            id = "test-loan",
            userId = "user-1",
            name = "Test Loan",
            originalAmount = 1000.0,
            remainingAmount = 800.0,
            interestRate = 5.0,
            monthlyPayment = 100.0,
            currency = "EUR",
            startDate = Date().time,
            estimatedPayoffDate = Date().time,
            loanType = LoanType.STUDENT_LOAN,
            lender = "Bank",
            isActive = true,
            createdAt = Date().time,
            updatedAt = Date().time,
            notes = ""
        )
        
        loanDao.insertLoan(loan)
        loanDao.updateRemainingAmount("test-loan", 700.0)
        
        val updated = loanDao.getLoanById("test-loan")
        assertEquals(700.0, updated?.remainingAmount, 0.01)
    }
}
