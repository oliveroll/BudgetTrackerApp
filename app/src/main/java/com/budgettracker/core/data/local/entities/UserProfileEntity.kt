package com.budgettracker.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.budgettracker.core.data.local.converters.Converters
import com.budgettracker.core.domain.model.UserProfile
import java.util.Date

/**
 * Room entity for UserProfile
 */
@Entity(tableName = "user_profiles")
@TypeConverters(Converters::class)
data class UserProfileEntity(
    @PrimaryKey
    val userId: String,
    val email: String,
    val name: String,
    val monthlyIncome: Double,
    val currency: String,
    val employmentStatus: String,
    val company: String,
    val baseSalary: Double,
    val state: String,
    val createdAt: Long,
    val updatedAt: Long,
    val profilePictureUrl: String?,
    val workAuthExpiryDate: Long?,
    val emergencyFundTarget: Double,
    val isOnboardingCompleted: Boolean,
    val syncStatus: String = "PENDING"
) {
    fun toDomainModel(): UserProfile {
        return UserProfile(
            userId = userId,
            email = email,
            name = name,
            monthlyIncome = monthlyIncome,
            currency = currency,
            employmentStatus = employmentStatus,
            company = company,
            baseSalary = baseSalary,
            state = state,
            createdAt = Date(createdAt),
            updatedAt = Date(updatedAt),
            profilePictureUrl = profilePictureUrl,
            workAuthExpiryDate = workAuthExpiryDate?.let { Date(it) },
            emergencyFundTarget = emergencyFundTarget,
            isOnboardingCompleted = isOnboardingCompleted
        )
    }
    
    companion object {
        fun fromDomainModel(userProfile: UserProfile, syncStatus: String = "PENDING"): UserProfileEntity {
            return UserProfileEntity(
                userId = userProfile.userId,
                email = userProfile.email,
                name = userProfile.name,
                monthlyIncome = userProfile.monthlyIncome,
                currency = userProfile.currency,
                employmentStatus = userProfile.employmentStatus,
                company = userProfile.company,
                baseSalary = userProfile.baseSalary,
                state = userProfile.state,
                createdAt = userProfile.createdAt.time,
                updatedAt = userProfile.updatedAt.time,
                profilePictureUrl = userProfile.profilePictureUrl,
                workAuthExpiryDate = userProfile.workAuthExpiryDate?.time,
                emergencyFundTarget = userProfile.emergencyFundTarget,
                isOnboardingCompleted = userProfile.isOnboardingCompleted,
                syncStatus = syncStatus
            )
        }
    }
}

