package com.budgettracker.core.data.local.converters

import androidx.room.TypeConverter
import com.budgettracker.core.domain.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

/**
 * Room type converters for complex data types
 */
class Converters {
    
    private val gson = Gson()
    
    // Date converters
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    // List<String> converters
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let {
            val listType = object : TypeToken<List<String>>() {}.type
            gson.fromJson<List<String>>(it, listType)
        } ?: emptyList()
    }
    
    // TransactionCategory converter
    @TypeConverter
    fun fromTransactionCategory(category: TransactionCategory?): String? {
        return category?.name
    }
    
    @TypeConverter
    fun toTransactionCategory(categoryName: String?): TransactionCategory? {
        return categoryName?.let { 
            try {
                TransactionCategory.valueOf(it)
            } catch (e: IllegalArgumentException) {
                TransactionCategory.MISCELLANEOUS
            }
        }
    }
    
    // TransactionType converter
    @TypeConverter
    fun fromTransactionType(type: TransactionType?): String? {
        return type?.name
    }
    
    @TypeConverter
    fun toTransactionType(typeName: String?): TransactionType? {
        return typeName?.let { 
            try {
                TransactionType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                TransactionType.EXPENSE
            }
        }
    }
    
    // RecurringPeriod converter
    @TypeConverter
    fun fromRecurringPeriod(period: RecurringPeriod?): String? {
        return period?.name
    }
    
    @TypeConverter
    fun toRecurringPeriod(periodName: String?): RecurringPeriod? {
        return periodName?.let { 
            try {
                RecurringPeriod.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
    
    // Priority converter
    @TypeConverter
    fun fromPriority(priority: Priority?): String? {
        return priority?.name
    }
    
    @TypeConverter
    fun toPriority(priorityName: String?): Priority? {
        return priorityName?.let { 
            try {
                Priority.valueOf(it)
            } catch (e: IllegalArgumentException) {
                Priority.MEDIUM
            }
        }
    }
    
    // GoalCategory converter
    @TypeConverter
    fun fromGoalCategory(category: GoalCategory?): String? {
        return category?.name
    }
    
    @TypeConverter
    fun toGoalCategory(categoryName: String?): GoalCategory? {
        return categoryName?.let { 
            try {
                GoalCategory.valueOf(it)
            } catch (e: IllegalArgumentException) {
                GoalCategory.OTHER
            }
        }
    }
    
    // List<CategoryBudget> converter for Budget entity
    @TypeConverter
    fun fromCategoryBudgetList(value: List<CategoryBudget>?): String? {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toCategoryBudgetList(value: String?): List<CategoryBudget>? {
        return value?.let {
            val listType = object : TypeToken<List<CategoryBudget>>() {}.type
            gson.fromJson<List<CategoryBudget>>(it, listType)
        } ?: emptyList()
    }
}

