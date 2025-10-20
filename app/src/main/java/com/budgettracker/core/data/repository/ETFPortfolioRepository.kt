package com.budgettracker.core.data.repository

import com.budgettracker.core.data.local.dao.ETFPortfolioDao
import com.budgettracker.core.data.local.entities.*
import com.budgettracker.core.domain.model.*
import com.budgettracker.core.utils.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for ETF Portfolio operations with Firebase sync
 */
@Singleton
class ETFPortfolioRepository @Inject constructor(
    private val etfPortfolioDao: ETFPortfolioDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    private val currentUserId: String?
        get() = auth.currentUser?.uid
    
    private val gson = Gson()
    
    // Portfolio Operations
    
    fun getActivePortfoliosFlow(): Flow<List<ETFPortfolio>> {
        val userId = currentUserId ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return etfPortfolioDao.getActivePortfoliosFlow(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    suspend fun getActivePortfolios(): Result<List<ETFPortfolio>> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val portfolios = etfPortfolioDao.getActivePortfolios(userId).map { it.toDomain() }
            Result.Success(portfolios)
        } catch (e: Exception) {
            Result.Error("Failed to get portfolios: ${e.message}")
        }
    }
    
    suspend fun getPortfolioById(portfolioId: String): Result<ETFPortfolio> {
        return try {
            val portfolio = etfPortfolioDao.getPortfolioById(portfolioId)?.toDomain()
                ?: return Result.Error("Portfolio not found")
            Result.Success(portfolio)
        } catch (e: Exception) {
            Result.Error("Failed to get portfolio: ${e.message}")
        }
    }
    
    suspend fun addPortfolio(portfolio: ETFPortfolio): Result<String> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val portfolioWithUser = portfolio.copy(userId = userId)
            val entity = ETFPortfolioEntity.fromDomain(portfolioWithUser, pendingSync = true)
            
            etfPortfolioDao.insertPortfolio(entity)
            syncPortfolioToFirebase(entity)
            
            Result.Success(portfolio.id)
        } catch (e: Exception) {
            Result.Error("Failed to add portfolio: ${e.message}")
        }
    }
    
    suspend fun updatePortfolio(portfolio: ETFPortfolio): Result<Unit> {
        return try {
            val entity = ETFPortfolioEntity.fromDomain(portfolio, pendingSync = true)
            etfPortfolioDao.updatePortfolio(entity)
            syncPortfolioToFirebase(entity)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to update portfolio: ${e.message}")
        }
    }
    
    suspend fun deletePortfolio(portfolioId: String): Result<Unit> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val portfolio = etfPortfolioDao.getPortfolioById(portfolioId) ?: return Result.Error("Portfolio not found")
            
            etfPortfolioDao.deletePortfolio(portfolio)
            
            firestore.collection("users")
                .document(userId)
                .collection("etfPortfolios")
                .document(portfolioId)
                .delete()
                .await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to delete portfolio: ${e.message}")
        }
    }
    
    // Holdings Operations
    
    fun getHoldingsFlow(portfolioId: String): Flow<List<ETFHolding>> {
        return etfPortfolioDao.getHoldingsFlow(portfolioId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    suspend fun getHoldings(portfolioId: String): Result<List<ETFHolding>> {
        return try {
            val holdings = etfPortfolioDao.getHoldings(portfolioId).map { it.toDomain() }
            Result.Success(holdings)
        } catch (e: Exception) {
            Result.Error("Failed to get holdings: ${e.message}")
        }
    }
    
    suspend fun addHolding(holding: ETFHolding): Result<String> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val entity = ETFHoldingEntity.fromDomain(holding, pendingSync = true)
            
            etfPortfolioDao.insertHolding(entity)
            syncHoldingToFirebase(entity, userId)
            
            Result.Success(holding.id)
        } catch (e: Exception) {
            Result.Error("Failed to add holding: ${e.message}")
        }
    }
    
    suspend fun updateHolding(holding: ETFHolding): Result<Unit> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val entity = ETFHoldingEntity.fromDomain(holding, pendingSync = true)
            
            etfPortfolioDao.updateHolding(entity)
            syncHoldingToFirebase(entity, userId)
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to update holding: ${e.message}")
        }
    }
    
    suspend fun deleteHolding(holdingId: String): Result<Unit> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val holding = etfPortfolioDao.getHoldingById(holdingId) ?: return Result.Error("Holding not found")
            
            etfPortfolioDao.deleteHolding(holding)
            
            firestore.collection("users")
                .document(userId)
                .collection("etfHoldings")
                .document(holdingId)
                .delete()
                .await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to delete holding: ${e.message}")
        }
    }
    
    // Transaction Operations
    
    suspend fun recordTransaction(
        portfolioId: String,
        holdingId: String,
        ticker: String,
        transactionType: InvestmentTransaction.TransactionType,
        shares: Double,
        pricePerShare: Double,
        fees: Double = 0.0
    ): Result<Unit> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val totalAmount = (shares * pricePerShare) + fees
            
            // Create transaction record
            val transaction = InvestmentTransactionEntity(
                portfolioId = portfolioId,
                holdingId = holdingId,
                ticker = ticker,
                transactionType = transactionType.name,
                shares = shares,
                pricePerShare = pricePerShare,
                totalAmount = totalAmount,
                fees = fees,
                pendingSync = true
            )
            etfPortfolioDao.insertTransaction(transaction)
            
            // Update holding
            val holding = etfPortfolioDao.getHoldingById(holdingId) ?: return Result.Error("Holding not found")
            val updatedHolding = when (transactionType) {
                InvestmentTransaction.TransactionType.BUY -> {
                    val newTotalShares = holding.sharesOwned + shares
                    val newCostBasis = holding.costBasis + totalAmount
                    holding.copy(
                        sharesOwned = newTotalShares,
                        costBasis = newCostBasis,
                        averageCostPerShare = newCostBasis / newTotalShares,
                        currentPrice = pricePerShare,
                        lastUpdatedPrice = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                        pendingSync = true
                    )
                }
                InvestmentTransaction.TransactionType.SELL -> {
                    val newTotalShares = (holding.sharesOwned - shares).coerceAtLeast(0.0)
                    val proportionSold = if (holding.sharesOwned > 0) shares / holding.sharesOwned else 0.0
                    val newCostBasis = holding.costBasis * (1 - proportionSold)
                    holding.copy(
                        sharesOwned = newTotalShares,
                        costBasis = newCostBasis,
                        averageCostPerShare = if (newTotalShares > 0) newCostBasis / newTotalShares else 0.0,
                        currentPrice = pricePerShare,
                        lastUpdatedPrice = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                        pendingSync = true
                    )
                }
                else -> holding
            }
            
            etfPortfolioDao.updateHolding(updatedHolding)
            
            // Sync to Firebase
            firestore.collection("users")
                .document(userId)
                .collection("investmentTransactions")
                .document(transaction.id)
                .set(transaction.toFirestoreMap())
                .await()
            
            syncHoldingToFirebase(updatedHolding, userId)
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to record transaction: ${e.message}")
        }
    }
    
    fun getTransactionsFlow(portfolioId: String): Flow<List<InvestmentTransactionEntity>> {
        return etfPortfolioDao.getTransactionsFlow(portfolioId)
    }
    
    suspend fun getRecentTransactions(portfolioId: String, limit: Int = 20): Result<List<InvestmentTransactionEntity>> {
        return try {
            val transactions = etfPortfolioDao.getRecentTransactions(portfolioId, limit)
            Result.Success(transactions)
        } catch (e: Exception) {
            Result.Error("Failed to get transactions: ${e.message}")
        }
    }
    
    // Firebase Sync
    
    suspend fun initializeFromFirebase() {
        try {
            val userId = currentUserId ?: return
            
            // Fetch portfolios
            val portfoliosSnapshot = firestore.collection("users")
                .document(userId)
                .collection("etfPortfolios")
                .get()
                .await()
            
            portfoliosSnapshot.documents.forEach { doc ->
                val entity = doc.toETFPortfolioEntity()
                etfPortfolioDao.insertPortfolio(entity)
            }
            
            // Fetch holdings
            val holdingsSnapshot = firestore.collection("users")
                .document(userId)
                .collection("etfHoldings")
                .get()
                .await()
            
            holdingsSnapshot.documents.forEach { doc ->
                val entity = doc.toETFHoldingEntity()
                etfPortfolioDao.insertHolding(entity)
            }
        } catch (e: Exception) {
            // Fail silently
        }
    }
    
    private suspend fun syncPortfolioToFirebase(portfolio: ETFPortfolioEntity) {
        try {
            val userId = currentUserId ?: return
            firestore.collection("users")
                .document(userId)
                .collection("etfPortfolios")
                .document(portfolio.id)
                .set(portfolio.toFirestoreMap())
                .await()
            
            etfPortfolioDao.markPortfolioSynced(portfolio.id, System.currentTimeMillis())
        } catch (e: Exception) {
            // Keep as pending sync
        }
    }
    
    private suspend fun syncHoldingToFirebase(holding: ETFHoldingEntity, userId: String) {
        try {
            firestore.collection("users")
                .document(userId)
                .collection("etfHoldings")
                .document(holding.id)
                .set(holding.toFirestoreMap())
                .await()
            
            etfPortfolioDao.markHoldingSynced(holding.id, System.currentTimeMillis())
        } catch (e: Exception) {
            // Keep as pending sync
        }
    }
}

// Extension functions for Firestore mapping
private fun ETFPortfolioEntity.toFirestoreMap(): Map<String, Any?> {
    val gson = Gson()
    return mapOf(
        "brokerageName" to brokerageName,
        "accountNumber" to accountNumber,
        "accountType" to accountType,
        "targetMonthlyInvestment" to targetMonthlyInvestment,
        "recurringInvestmentFrequency" to recurringInvestmentFrequency,
        "recurringInvestmentDayOfMonth" to recurringInvestmentDayOfMonth,
        "autoInvest" to autoInvest,
        "targetAllocation" to gson.toJson(targetAllocation),
        "rebalanceThreshold" to rebalanceThreshold,
        "investmentHorizon" to investmentHorizon,
        "riskTolerance" to riskTolerance,
        "notes" to notes,
        "isActive" to isActive,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

private fun ETFHoldingEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "portfolioId" to portfolioId,
    "ticker" to ticker,
    "name" to name,
    "sharesOwned" to sharesOwned,
    "currentPrice" to currentPrice,
    "costBasis" to costBasis,
    "averageCostPerShare" to averageCostPerShare,
    "assetClass" to assetClass,
    "expenseRatio" to expenseRatio,
    "dividendYield" to dividendYield,
    "lastUpdatedPrice" to com.google.firebase.Timestamp(Date(lastUpdatedPrice)),
    "notes" to notes,
    "createdAt" to FieldValue.serverTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp()
)

private fun InvestmentTransactionEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "portfolioId" to portfolioId,
    "holdingId" to holdingId,
    "ticker" to ticker,
    "transactionType" to transactionType,
    "shares" to shares,
    "pricePerShare" to pricePerShare,
    "totalAmount" to totalAmount,
    "fees" to fees,
    "transactionDate" to com.google.firebase.Timestamp(Date(transactionDate)),
    "notes" to notes,
    "createdAt" to FieldValue.serverTimestamp()
)

private fun com.google.firebase.firestore.DocumentSnapshot.toETFPortfolioEntity(): ETFPortfolioEntity {
    val gson = Gson()
    val targetAllocationJson = getString("targetAllocation") ?: "{}"
    val type = object : TypeToken<Map<String, Double>>() {}.type
    val targetAllocation: Map<String, Double> = gson.fromJson(targetAllocationJson, type) ?: emptyMap()
    
    return ETFPortfolioEntity(
        id = id,
        userId = getString("userId") ?: "",
        brokerageName = getString("brokerageName") ?: "",
        accountNumber = getString("accountNumber") ?: "",
        accountType = getString("accountType") ?: "Brokerage",
        targetMonthlyInvestment = getDouble("targetMonthlyInvestment") ?: 0.0,
        recurringInvestmentFrequency = getString("recurringInvestmentFrequency") ?: "BIWEEKLY",
        recurringInvestmentDayOfMonth = getLong("recurringInvestmentDayOfMonth")?.toInt(),
        autoInvest = getBoolean("autoInvest") ?: false,
        targetAllocation = targetAllocation,
        rebalanceThreshold = getDouble("rebalanceThreshold") ?: 5.0,
        investmentHorizon = getString("investmentHorizon") ?: "LONG_TERM",
        riskTolerance = getString("riskTolerance") ?: "MODERATE",
        notes = getString("notes") ?: "",
        isActive = getBoolean("isActive") ?: true,
        createdAt = getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
        updatedAt = getTimestamp("updatedAt")?.toDate()?.time ?: System.currentTimeMillis(),
        syncedAt = System.currentTimeMillis(),
        pendingSync = false
    )
}

private fun com.google.firebase.firestore.DocumentSnapshot.toETFHoldingEntity(): ETFHoldingEntity {
    return ETFHoldingEntity(
        id = id,
        portfolioId = getString("portfolioId") ?: "",
        ticker = getString("ticker") ?: "",
        name = getString("name") ?: "",
        sharesOwned = getDouble("sharesOwned") ?: 0.0,
        currentPrice = getDouble("currentPrice") ?: 0.0,
        costBasis = getDouble("costBasis") ?: 0.0,
        averageCostPerShare = getDouble("averageCostPerShare") ?: 0.0,
        assetClass = getString("assetClass") ?: "US_EQUITY",
        expenseRatio = getDouble("expenseRatio") ?: 0.0,
        dividendYield = getDouble("dividendYield") ?: 0.0,
        lastUpdatedPrice = getTimestamp("lastUpdatedPrice")?.toDate()?.time ?: System.currentTimeMillis(),
        notes = getString("notes") ?: "",
        createdAt = getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
        updatedAt = getTimestamp("updatedAt")?.toDate()?.time ?: System.currentTimeMillis(),
        syncedAt = System.currentTimeMillis(),
        pendingSync = false
    )
}

