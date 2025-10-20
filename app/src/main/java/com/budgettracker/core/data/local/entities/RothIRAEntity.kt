package com.budgettracker.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.budgettracker.core.domain.model.ContributionFrequency
import com.budgettracker.core.domain.model.RothIRA
import java.util.*

/**
 * Room entity for Roth IRA account data with Firebase sync support
 */
@Entity(tableName = "roth_iras")
data class RothIRAEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val brokerageName: String = "",
    val accountNumber: String = "",
    val annualContributionLimit: Double = 7000.0,
    val contributionsThisYear: Double = 0.0,
    val currentBalance: Double = 0.0,
    val taxYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val recurringContributionAmount: Double? = null,
    val recurringContributionFrequency: String = ContributionFrequency.BIWEEKLY.name,
    val recurringContributionStartDate: Long? = null,
    val recurringContributionDayOfMonth: Int? = null,
    val autoIncrease: Boolean = false,
    val autoIncreasePercentage: Double = 0.0,
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
    fun toDomain(): RothIRA {
        return RothIRA(
            id = id,
            userId = userId,
            brokerageName = brokerageName,
            accountNumber = accountNumber,
            annualContributionLimit = annualContributionLimit,
            contributionsThisYear = contributionsThisYear,
            currentBalance = currentBalance,
            taxYear = taxYear,
            recurringContributionAmount = recurringContributionAmount,
            recurringContributionFrequency = ContributionFrequency.valueOf(recurringContributionFrequency),
            recurringContributionStartDate = recurringContributionStartDate?.let { Date(it) },
            recurringContributionDayOfMonth = recurringContributionDayOfMonth,
            autoIncrease = autoIncrease,
            autoIncreasePercentage = autoIncreasePercentage,
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
        fun fromDomain(ira: RothIRA, pendingSync: Boolean = true): RothIRAEntity {
            return RothIRAEntity(
                id = ira.id,
                userId = ira.userId,
                brokerageName = ira.brokerageName,
                accountNumber = ira.accountNumber,
                annualContributionLimit = ira.annualContributionLimit,
                contributionsThisYear = ira.contributionsThisYear,
                currentBalance = ira.currentBalance,
                taxYear = ira.taxYear,
                recurringContributionAmount = ira.recurringContributionAmount,
                recurringContributionFrequency = ira.recurringContributionFrequency.name,
                recurringContributionStartDate = ira.recurringContributionStartDate?.time,
                recurringContributionDayOfMonth = ira.recurringContributionDayOfMonth,
                autoIncrease = ira.autoIncrease,
                autoIncreasePercentage = ira.autoIncreasePercentage,
                notes = ira.notes,
                isActive = ira.isActive,
                createdAt = ira.createdAt.time,
                updatedAt = ira.updatedAt.time,
                pendingSync = pendingSync
            )
        }
    }
}

/**
 * Room entity for IRA contribution records
 */
@Entity(tableName = "ira_contributions")
data class IRAContributionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val iraId: String = "",
    val amount: Double = 0.0,
    val contributionDate: Long = System.currentTimeMillis(),
    val taxYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val isAutomatic: Boolean = false,
    val transactionId: String? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null,
    val pendingSync: Boolean = false
)

