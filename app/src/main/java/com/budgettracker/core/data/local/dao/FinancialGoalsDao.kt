package com.budgettracker.core.data.local.dao

import androidx.room.*
import com.budgettracker.core.data.local.entities.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Debt Loan operations
 */
@Dao
interface DebtLoanDao {
    
    @Query("SELECT * FROM debt_loans WHERE userId = :userId AND isActive = 1 ORDER BY createdAt DESC")
    fun getActiveLoansFlow(userId: String): Flow<List<DebtLoanEntity>>
    
    @Query("SELECT * FROM debt_loans WHERE userId = :userId AND isActive = 1 ORDER BY createdAt DESC")
    suspend fun getActiveLoans(userId: String): List<DebtLoanEntity>
    
    @Query("SELECT * FROM debt_loans WHERE id = :loanId")
    suspend fun getLoanById(loanId: String): DebtLoanEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: DebtLoanEntity)
    
    @Update
    suspend fun updateLoan(loan: DebtLoanEntity)
    
    @Delete
    suspend fun deleteLoan(loan: DebtLoanEntity)
    
    @Query("DELETE FROM debt_loans WHERE id = :loanId")
    suspend fun deleteLoanById(loanId: String)
    
    @Query("SELECT * FROM debt_loans WHERE pendingSync = 1")
    suspend fun getPendingSyncLoans(): List<DebtLoanEntity>
    
    @Query("UPDATE debt_loans SET pendingSync = 0, syncedAt = :syncedAt WHERE id = :loanId")
    suspend fun markSynced(loanId: String, syncedAt: Long)
    
    // Payment records
    @Query("SELECT * FROM debt_payment_records WHERE loanId = :loanId ORDER BY paymentDate DESC")
    fun getPaymentRecordsFlow(loanId: String): Flow<List<DebtPaymentRecordEntity>>
    
    @Query("SELECT * FROM debt_payment_records WHERE loanId = :loanId ORDER BY paymentDate DESC")
    suspend fun getPaymentRecords(loanId: String): List<DebtPaymentRecordEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentRecord(record: DebtPaymentRecordEntity)
    
    @Delete
    suspend fun deletePaymentRecord(record: DebtPaymentRecordEntity)
}

/**
 * DAO for Roth IRA operations
 */
@Dao
interface RothIRADao {
    
    @Query("SELECT * FROM roth_iras WHERE userId = :userId AND isActive = 1 ORDER BY createdAt DESC")
    fun getActiveIRAsFlow(userId: String): Flow<List<RothIRAEntity>>
    
    @Query("SELECT * FROM roth_iras WHERE userId = :userId AND isActive = 1 ORDER BY createdAt DESC")
    suspend fun getActiveIRAs(userId: String): List<RothIRAEntity>
    
    @Query("SELECT * FROM roth_iras WHERE id = :iraId")
    suspend fun getIRAById(iraId: String): RothIRAEntity?
    
    @Query("SELECT * FROM roth_iras WHERE userId = :userId AND taxYear = :taxYear AND isActive = 1 LIMIT 1")
    suspend fun getIRAForYear(userId: String, taxYear: Int): RothIRAEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIRA(ira: RothIRAEntity)
    
    @Update
    suspend fun updateIRA(ira: RothIRAEntity)
    
    @Delete
    suspend fun deleteIRA(ira: RothIRAEntity)
    
    @Query("SELECT * FROM roth_iras WHERE pendingSync = 1")
    suspend fun getPendingSyncIRAs(): List<RothIRAEntity>
    
    @Query("UPDATE roth_iras SET pendingSync = 0, syncedAt = :syncedAt WHERE id = :iraId")
    suspend fun markSynced(iraId: String, syncedAt: Long)
    
    // Contribution records
    @Query("SELECT * FROM ira_contributions WHERE iraId = :iraId ORDER BY contributionDate DESC")
    fun getContributionsFlow(iraId: String): Flow<List<IRAContributionEntity>>
    
    @Query("SELECT * FROM ira_contributions WHERE iraId = :iraId AND taxYear = :taxYear ORDER BY contributionDate DESC")
    suspend fun getContributionsForYear(iraId: String, taxYear: Int): List<IRAContributionEntity>
    
    @Query("SELECT SUM(amount) FROM ira_contributions WHERE iraId = :iraId AND taxYear = :taxYear")
    suspend fun getTotalContributionsForYear(iraId: String, taxYear: Int): Double?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContribution(contribution: IRAContributionEntity)
    
    @Delete
    suspend fun deleteContribution(contribution: IRAContributionEntity)
}

/**
 * DAO for Emergency Fund operations
 */
@Dao
interface EmergencyFundDao {
    
    @Query("SELECT * FROM emergency_funds WHERE userId = :userId AND isActive = 1 ORDER BY createdAt DESC")
    fun getActiveFundsFlow(userId: String): Flow<List<EmergencyFundEntity>>
    
    @Query("SELECT * FROM emergency_funds WHERE userId = :userId AND isActive = 1 ORDER BY createdAt DESC")
    suspend fun getActiveFunds(userId: String): List<EmergencyFundEntity>
    
    @Query("SELECT * FROM emergency_funds WHERE id = :fundId")
    suspend fun getFundById(fundId: String): EmergencyFundEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFund(fund: EmergencyFundEntity)
    
    @Update
    suspend fun updateFund(fund: EmergencyFundEntity)
    
    @Delete
    suspend fun deleteFund(fund: EmergencyFundEntity)
    
    @Query("SELECT * FROM emergency_funds WHERE pendingSync = 1")
    suspend fun getPendingSyncFunds(): List<EmergencyFundEntity>
    
    @Query("UPDATE emergency_funds SET pendingSync = 0, syncedAt = :syncedAt WHERE id = :fundId")
    suspend fun markSynced(fundId: String, syncedAt: Long)
    
    // Deposit records
    @Query("SELECT * FROM emergency_fund_deposits WHERE fundId = :fundId ORDER BY depositDate DESC")
    fun getDepositsFlow(fundId: String): Flow<List<EmergencyFundDepositEntity>>
    
    @Query("SELECT * FROM emergency_fund_deposits WHERE fundId = :fundId ORDER BY depositDate DESC LIMIT :limit")
    suspend fun getRecentDeposits(fundId: String, limit: Int = 10): List<EmergencyFundDepositEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeposit(deposit: EmergencyFundDepositEntity)
    
    @Delete
    suspend fun deleteDeposit(deposit: EmergencyFundDepositEntity)
}

/**
 * DAO for ETF Portfolio operations
 */
@Dao
interface ETFPortfolioDao {
    
    @Query("SELECT * FROM etf_portfolios WHERE userId = :userId AND isActive = 1 ORDER BY createdAt DESC")
    fun getActivePortfoliosFlow(userId: String): Flow<List<ETFPortfolioEntity>>
    
    @Query("SELECT * FROM etf_portfolios WHERE userId = :userId AND isActive = 1 ORDER BY createdAt DESC")
    suspend fun getActivePortfolios(userId: String): List<ETFPortfolioEntity>
    
    @Query("SELECT * FROM etf_portfolios WHERE id = :portfolioId")
    suspend fun getPortfolioById(portfolioId: String): ETFPortfolioEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPortfolio(portfolio: ETFPortfolioEntity)
    
    @Update
    suspend fun updatePortfolio(portfolio: ETFPortfolioEntity)
    
    @Delete
    suspend fun deletePortfolio(portfolio: ETFPortfolioEntity)
    
    @Query("SELECT * FROM etf_portfolios WHERE pendingSync = 1")
    suspend fun getPendingSyncPortfolios(): List<ETFPortfolioEntity>
    
    @Query("UPDATE etf_portfolios SET pendingSync = 0, syncedAt = :syncedAt WHERE id = :portfolioId")
    suspend fun markPortfolioSynced(portfolioId: String, syncedAt: Long)
    
    // Holdings
    @Query("SELECT * FROM etf_holdings WHERE portfolioId = :portfolioId ORDER BY currentPrice * sharesOwned DESC")
    fun getHoldingsFlow(portfolioId: String): Flow<List<ETFHoldingEntity>>
    
    @Query("SELECT * FROM etf_holdings WHERE portfolioId = :portfolioId ORDER BY currentPrice * sharesOwned DESC")
    suspend fun getHoldings(portfolioId: String): List<ETFHoldingEntity>
    
    @Query("SELECT * FROM etf_holdings WHERE id = :holdingId")
    suspend fun getHoldingById(holdingId: String): ETFHoldingEntity?
    
    @Query("SELECT * FROM etf_holdings WHERE portfolioId = :portfolioId AND ticker = :ticker")
    suspend fun getHoldingByTicker(portfolioId: String, ticker: String): ETFHoldingEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolding(holding: ETFHoldingEntity)
    
    @Update
    suspend fun updateHolding(holding: ETFHoldingEntity)
    
    @Delete
    suspend fun deleteHolding(holding: ETFHoldingEntity)
    
    @Query("SELECT * FROM etf_holdings WHERE pendingSync = 1")
    suspend fun getPendingSyncHoldings(): List<ETFHoldingEntity>
    
    @Query("UPDATE etf_holdings SET pendingSync = 0, syncedAt = :syncedAt WHERE id = :holdingId")
    suspend fun markHoldingSynced(holdingId: String, syncedAt: Long)
    
    // Transactions
    @Query("SELECT * FROM investment_transactions WHERE portfolioId = :portfolioId ORDER BY transactionDate DESC")
    fun getTransactionsFlow(portfolioId: String): Flow<List<InvestmentTransactionEntity>>
    
    @Query("SELECT * FROM investment_transactions WHERE portfolioId = :portfolioId ORDER BY transactionDate DESC LIMIT :limit")
    suspend fun getRecentTransactions(portfolioId: String, limit: Int = 20): List<InvestmentTransactionEntity>
    
    @Query("SELECT * FROM investment_transactions WHERE holdingId = :holdingId ORDER BY transactionDate DESC")
    suspend fun getTransactionsForHolding(holdingId: String): List<InvestmentTransactionEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: InvestmentTransactionEntity)
    
    @Delete
    suspend fun deleteTransaction(transaction: InvestmentTransactionEntity)
}

