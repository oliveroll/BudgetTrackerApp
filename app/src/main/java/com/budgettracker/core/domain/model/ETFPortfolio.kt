package com.budgettracker.core.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

/**
 * ETF Portfolio model for investment tracking
 */
@Parcelize
data class ETFPortfolio(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val brokerageName: String = "",
    val accountNumber: String = "",
    val accountType: String = "Brokerage", // Brokerage, IRA, 401k, etc.
    val targetMonthlyInvestment: Double = 0.0,
    val recurringInvestmentFrequency: InvestmentFrequency = InvestmentFrequency.BIWEEKLY,
    val recurringInvestmentDayOfMonth: Int? = null,
    val autoInvest: Boolean = false,
    val targetAllocation: Map<String, Double> = emptyMap(), // ticker -> percentage
    val rebalanceThreshold: Double = 5.0, // Percentage deviation before rebalancing
    val investmentHorizon: InvestmentHorizon = InvestmentHorizon.LONG_TERM,
    val riskTolerance: RiskTolerance = RiskTolerance.MODERATE,
    val notes: String = "",
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) : Parcelable {
    
    /**
     * Calculate total portfolio value from holdings
     */
    fun getTotalPortfolioValue(holdings: List<ETFHolding>): Double {
        return holdings.filter { it.portfolioId == id }.sumOf { it.getCurrentValue() }
    }
    
    /**
     * Calculate total cost basis
     */
    fun getTotalCostBasis(holdings: List<ETFHolding>): Double {
        return holdings.filter { it.portfolioId == id }.sumOf { it.costBasis }
    }
    
    /**
     * Calculate total gain/loss
     */
    fun getTotalGainLoss(holdings: List<ETFHolding>): Double {
        val holdings = holdings.filter { it.portfolioId == id }
        return getTotalPortfolioValue(holdings) - getTotalCostBasis(holdings)
    }
    
    /**
     * Calculate gain/loss percentage
     */
    fun getGainLossPercentage(holdings: List<ETFHolding>): Float {
        val costBasis = getTotalCostBasis(holdings)
        return if (costBasis > 0) {
            (getTotalGainLoss(holdings) / costBasis * 100).toFloat()
        } else 0f
    }
    
    /**
     * Calculate current allocation
     */
    fun getCurrentAllocation(holdings: List<ETFHolding>): Map<String, Double> {
        val portfolioHoldings = holdings.filter { it.portfolioId == id }
        val totalValue = getTotalPortfolioValue(portfolioHoldings)
        
        if (totalValue <= 0) return emptyMap()
        
        return portfolioHoldings.associate { holding ->
            holding.ticker to (holding.getCurrentValue() / totalValue * 100)
        }
    }
    
    /**
     * Check if rebalancing is needed
     */
    fun needsRebalancing(holdings: List<ETFHolding>): Boolean {
        val currentAllocation = getCurrentAllocation(holdings)
        
        return targetAllocation.any { (ticker, targetPercent) ->
            val currentPercent = currentAllocation[ticker] ?: 0.0
            kotlin.math.abs(currentPercent - targetPercent) > rebalanceThreshold
        }
    }
    
    /**
     * Calculate suggested rebalancing trades
     */
    fun suggestRebalancing(holdings: List<ETFHolding>): Map<String, Double> {
        val currentAllocation = getCurrentAllocation(holdings)
        val totalValue = getTotalPortfolioValue(holdings)
        
        return targetAllocation.mapValues { (ticker, targetPercent) ->
            val currentPercent = currentAllocation[ticker] ?: 0.0
            val targetValue = totalValue * (targetPercent / 100)
            val currentValue = totalValue * (currentPercent / 100)
            targetValue - currentValue // Positive = buy, negative = sell
        }
    }
    
    /**
     * Calculate allocation for next investment based on target
     */
    fun suggestNextInvestment(amount: Double, holdings: List<ETFHolding>): Map<String, Double> {
        val currentAllocation = getCurrentAllocation(holdings)
        val totalValue = getTotalPortfolioValue(holdings)
        val newTotalValue = totalValue + amount
        
        return targetAllocation.mapValues { (ticker, targetPercent) ->
            val targetValueAfter = newTotalValue * (targetPercent / 100)
            val currentValue = totalValue * ((currentAllocation[ticker] ?: 0.0) / 100)
            (targetValueAfter - currentValue).coerceAtLeast(0.0)
        }
    }
}

/**
 * Individual ETF holding within a portfolio
 */
@Parcelize
data class ETFHolding(
    val id: String = UUID.randomUUID().toString(),
    val portfolioId: String = "",
    val ticker: String = "",
    val name: String = "",
    val sharesOwned: Double = 0.0,
    val currentPrice: Double = 0.0,
    val costBasis: Double = 0.0, // Total amount invested
    val averageCostPerShare: Double = 0.0,
    val assetClass: AssetClass = AssetClass.US_EQUITY,
    val expenseRatio: Double = 0.0, // Annual expense ratio percentage
    val dividendYield: Double = 0.0,
    val lastUpdatedPrice: Date = Date(),
    val notes: String = "",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) : Parcelable {
    
    /**
     * Calculate current value of holding
     */
    fun getCurrentValue(): Double {
        return sharesOwned * currentPrice
    }
    
    /**
     * Calculate gain/loss
     */
    fun getGainLoss(): Double {
        return getCurrentValue() - costBasis
    }
    
    /**
     * Calculate gain/loss percentage
     */
    fun getGainLossPercentage(): Float {
        return if (costBasis > 0) {
            (getGainLoss() / costBasis * 100).toFloat()
        } else 0f
    }
    
    /**
     * Calculate annual dividend income
     */
    fun getAnnualDividendIncome(): Double {
        return getCurrentValue() * (dividendYield / 100)
    }
}

/**
 * Investment transaction record
 */
@Parcelize
data class InvestmentTransaction(
    val id: String = UUID.randomUUID().toString(),
    val portfolioId: String = "",
    val holdingId: String = "",
    val ticker: String = "",
    val transactionType: TransactionType = TransactionType.BUY,
    val shares: Double = 0.0,
    val pricePerShare: Double = 0.0,
    val totalAmount: Double = 0.0,
    val fees: Double = 0.0,
    val transactionDate: Date = Date(),
    val notes: String = ""
) : Parcelable {
    
    enum class TransactionType {
        BUY, SELL, DIVIDEND, FEE, TRANSFER_IN, TRANSFER_OUT
    }
}

/**
 * Investment frequency options
 */
@Parcelize
enum class InvestmentFrequency(val displayName: String, val perYear: Int) : Parcelable {
    WEEKLY("Weekly", 52),
    BIWEEKLY("Biweekly", 26),
    MONTHLY("Monthly", 12),
    QUARTERLY("Quarterly", 4),
    ANNUAL("Annual", 1),
    NONE("None", 0)
}

/**
 * Investment horizon
 */
@Parcelize
enum class InvestmentHorizon(val displayName: String, val years: Int) : Parcelable {
    SHORT_TERM("Short-term (< 5 years)", 5),
    MEDIUM_TERM("Medium-term (5-10 years)", 10),
    LONG_TERM("Long-term (10+ years)", 30)
}

/**
 * Risk tolerance levels
 */
@Parcelize
enum class RiskTolerance(val displayName: String, val description: String) : Parcelable {
    CONSERVATIVE("Conservative", "Minimize risk, slower growth"),
    MODERATE("Moderate", "Balanced risk and growth"),
    AGGRESSIVE("Aggressive", "Higher risk, higher potential returns")
}

/**
 * Asset class categories
 */
@Parcelize
enum class AssetClass(
    val displayName: String,
    val description: String,
    val color: String
) : Parcelable {
    US_EQUITY("U.S. Equity", "U.S. Stock Market (VOO, VTI)", "#28a745"),
    INTERNATIONAL_EQUITY("International Equity", "International Stocks (VXUS)", "#17a2b8"),
    BONDS("Bonds", "Fixed Income Securities (BND)", "#6f42c1"),
    REAL_ESTATE("Real Estate", "REITs (VNQ)", "#fd7e14"),
    COMMODITIES("Commodities", "Gold, Oil, etc.", "#ffc107"),
    CASH("Cash", "Money Market, Cash Equivalents", "#6c757d"),
    CRYPTO("Cryptocurrency", "Bitcoin, Ethereum, etc.", "#e83e8c"),
    OTHER("Other", "Other Asset Types", "#343a40")
}

/**
 * Portfolio allocation suggestion
 */
data class AllocationSuggestion(
    val ticker: String,
    val currentPercent: Double,
    val targetPercent: Double,
    val deviationPercent: Double,
    val currentValue: Double,
    val targetValue: Double,
    val actionNeeded: String, // "Buy $X", "Sell $X", "Hold"
    val priority: Int // 1 = high priority, 3 = low priority
)

/**
 * Portfolio performance metrics
 */
data class PortfolioPerformance(
    val totalValue: Double,
    val totalCostBasis: Double,
    val totalGainLoss: Double,
    val gainLossPercentage: Float,
    val annualDividendIncome: Double,
    val annualExpenseRatio: Double,
    val diversificationScore: Float, // 0-100, higher is better
    val needsRebalancing: Boolean,
    val allocationDeviations: Map<String, Double>
)

