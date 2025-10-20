package com.budgettracker.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.budgettracker.core.domain.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

/**
 * Room entity for ETF portfolio data with Firebase sync support
 */
@Entity(tableName = "etf_portfolios")
@TypeConverters(MapTypeConverter::class)
data class ETFPortfolioEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val brokerageName: String = "",
    val accountNumber: String = "",
    val accountType: String = "Brokerage",
    val targetMonthlyInvestment: Double = 0.0,
    val recurringInvestmentFrequency: String = InvestmentFrequency.BIWEEKLY.name,
    val recurringInvestmentDayOfMonth: Int? = null,
    val autoInvest: Boolean = false,
    val targetAllocation: Map<String, Double> = emptyMap(),
    val rebalanceThreshold: Double = 5.0,
    val investmentHorizon: String = InvestmentHorizon.LONG_TERM.name,
    val riskTolerance: String = RiskTolerance.MODERATE.name,
    val notes: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null,
    val pendingSync: Boolean = false
) {
    
    /**
     * Convert to domain model
     */
    fun toDomain(): ETFPortfolio {
        return ETFPortfolio(
            id = id,
            userId = userId,
            brokerageName = brokerageName,
            accountNumber = accountNumber,
            accountType = accountType,
            targetMonthlyInvestment = targetMonthlyInvestment,
            recurringInvestmentFrequency = InvestmentFrequency.valueOf(recurringInvestmentFrequency),
            recurringInvestmentDayOfMonth = recurringInvestmentDayOfMonth,
            autoInvest = autoInvest,
            targetAllocation = targetAllocation,
            rebalanceThreshold = rebalanceThreshold,
            investmentHorizon = InvestmentHorizon.valueOf(investmentHorizon),
            riskTolerance = RiskTolerance.valueOf(riskTolerance),
            notes = notes,
            isActive = isActive,
            createdAt = Date(createdAt),
            updatedAt = Date(updatedAt)
        )
    }
    
    companion object {
        /**
         * Create entity from domain model
         */
        fun fromDomain(portfolio: ETFPortfolio, pendingSync: Boolean = true): ETFPortfolioEntity {
            return ETFPortfolioEntity(
                id = portfolio.id,
                userId = portfolio.userId,
                brokerageName = portfolio.brokerageName,
                accountNumber = portfolio.accountNumber,
                accountType = portfolio.accountType,
                targetMonthlyInvestment = portfolio.targetMonthlyInvestment,
                recurringInvestmentFrequency = portfolio.recurringInvestmentFrequency.name,
                recurringInvestmentDayOfMonth = portfolio.recurringInvestmentDayOfMonth,
                autoInvest = portfolio.autoInvest,
                targetAllocation = portfolio.targetAllocation,
                rebalanceThreshold = portfolio.rebalanceThreshold,
                investmentHorizon = portfolio.investmentHorizon.name,
                riskTolerance = portfolio.riskTolerance.name,
                notes = portfolio.notes,
                isActive = portfolio.isActive,
                createdAt = portfolio.createdAt.time,
                updatedAt = portfolio.updatedAt.time,
                pendingSync = pendingSync
            )
        }
    }
}

/**
 * Room entity for ETF holdings
 */
@Entity(tableName = "etf_holdings")
data class ETFHoldingEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val portfolioId: String = "",
    val ticker: String = "",
    val name: String = "",
    val sharesOwned: Double = 0.0,
    val currentPrice: Double = 0.0,
    val costBasis: Double = 0.0,
    val averageCostPerShare: Double = 0.0,
    val assetClass: String = AssetClass.US_EQUITY.name,
    val expenseRatio: Double = 0.0,
    val dividendYield: Double = 0.0,
    val lastUpdatedPrice: Long = System.currentTimeMillis(),
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null,
    val pendingSync: Boolean = false
) {
    
    /**
     * Convert to domain model
     */
    fun toDomain(): ETFHolding {
        return ETFHolding(
            id = id,
            portfolioId = portfolioId,
            ticker = ticker,
            name = name,
            sharesOwned = sharesOwned,
            currentPrice = currentPrice,
            costBasis = costBasis,
            averageCostPerShare = averageCostPerShare,
            assetClass = AssetClass.valueOf(assetClass),
            expenseRatio = expenseRatio,
            dividendYield = dividendYield,
            lastUpdatedPrice = Date(lastUpdatedPrice),
            notes = notes,
            createdAt = Date(createdAt),
            updatedAt = Date(updatedAt)
        )
    }
    
    companion object {
        /**
         * Create entity from domain model
         */
        fun fromDomain(holding: ETFHolding, pendingSync: Boolean = true): ETFHoldingEntity {
            return ETFHoldingEntity(
                id = holding.id,
                portfolioId = holding.portfolioId,
                ticker = holding.ticker,
                name = holding.name,
                sharesOwned = holding.sharesOwned,
                currentPrice = holding.currentPrice,
                costBasis = holding.costBasis,
                averageCostPerShare = holding.averageCostPerShare,
                assetClass = holding.assetClass.name,
                expenseRatio = holding.expenseRatio,
                dividendYield = holding.dividendYield,
                lastUpdatedPrice = holding.lastUpdatedPrice.time,
                notes = holding.notes,
                createdAt = holding.createdAt.time,
                updatedAt = holding.updatedAt.time,
                pendingSync = pendingSync
            )
        }
    }
}

/**
 * Room entity for investment transactions
 */
@Entity(tableName = "investment_transactions")
data class InvestmentTransactionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val portfolioId: String = "",
    val holdingId: String = "",
    val ticker: String = "",
    val transactionType: String = InvestmentTransaction.TransactionType.BUY.name,
    val shares: Double = 0.0,
    val pricePerShare: Double = 0.0,
    val totalAmount: Double = 0.0,
    val fees: Double = 0.0,
    val transactionDate: Long = System.currentTimeMillis(),
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null,
    val pendingSync: Boolean = false
)

/**
 * Type converter for Map<String, Double>
 */
class MapTypeConverter {
    private val gson = Gson()
    
    @TypeConverter
    fun fromMap(map: Map<String, Double>?): String {
        return gson.toJson(map ?: emptyMap<String, Double>())
    }
    
    @TypeConverter
    fun toMap(json: String): Map<String, Double> {
        val type = object : TypeToken<Map<String, Double>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }
}

