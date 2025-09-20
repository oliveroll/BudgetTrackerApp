package com.budgettracker.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Extension functions for common operations
 */

/**
 * Format double as currency string
 */
fun Double.toCurrencyString(currencyCode: String = "USD"): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    formatter.currency = java.util.Currency.getInstance(currencyCode)
    return formatter.format(this)
}

/**
 * Format double as percentage string
 */
fun Double.toPercentageString(decimals: Int = 1): String {
    return String.format("%.${decimals}f%%", this)
}

/**
 * Format float as percentage string
 */
fun Float.toPercentageString(decimals: Int = 1): String {
    return String.format("%.${decimals}f%%", this)
}

/**
 * Format double with commas for large numbers
 */
fun Double.toFormattedString(decimals: Int = 2): String {
    return String.format("%,.${decimals}f", this)
}

/**
 * Get absolute value as currency string
 */
fun Double.toAbsCurrencyString(currencyCode: String = "USD"): String {
    return abs(this).toCurrencyString(currencyCode)
}

/**
 * Format date to display string
 */
fun Date.toDisplayString(pattern: String = Constants.DATE_FORMAT_DISPLAY): String {
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(this)
}

/**
 * Format date to month-year string
 */
fun Date.toMonthYearString(): String {
    val formatter = SimpleDateFormat(Constants.DATE_FORMAT_MONTH_YEAR, Locale.getDefault())
    return formatter.format(this)
}

/**
 * Check if date is in current month
 */
fun Date.isCurrentMonth(): Boolean {
    val now = Date()
    return this.month == now.month && this.year == now.year
}

/**
 * Check if date is today
 */
fun Date.isToday(): Boolean {
    val now = Date()
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(this) == formatter.format(now)
}

/**
 * Get relative time string (e.g., "2 days ago", "in 3 days")
 */
fun Date.toRelativeTimeString(): String {
    val now = Date()
    val diffInMillis = this.time - now.time
    val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)
    
    return when {
        diffInDays == 0L -> "Today"
        diffInDays == 1L -> "Tomorrow"
        diffInDays == -1L -> "Yesterday"
        diffInDays > 1 -> "In ${diffInDays} days"
        diffInDays < -1 -> "${abs(diffInDays)} days ago"
        else -> toDisplayString()
    }
}

/**
 * Convert hex color string to Compose Color
 */
fun String.toComposeColor(): Color {
    return try {
        Color(android.graphics.Color.parseColor(this))
    } catch (e: IllegalArgumentException) {
        Color.Gray // Default color if parsing fails
    }
}

/**
 * Capitalize first letter of each word
 */
fun String.toTitleCase(): String {
    return this.split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
        }
    }
}

/**
 * Validate email format
 */
fun String.isValidEmail(): Boolean {
    return this.matches(Regex(Constants.EMAIL_PATTERN))
}

/**
 * Validate phone number format
 */
fun String.isValidPhone(): Boolean {
    return this.matches(Regex(Constants.PHONE_PATTERN))
}

/**
 * Convert string to double safely
 */
fun String.toDoubleOrZero(): Double {
    return try {
        this.replace(",", "").toDouble()
    } catch (e: NumberFormatException) {
        0.0
    }
}

/**
 * Truncate string to specified length with ellipsis
 */
fun String.truncate(maxLength: Int): String {
    return if (this.length <= maxLength) {
        this
    } else {
        "${this.substring(0, maxLength - 3)}..."
    }
}

/**
 * Convert currency between EUR and USD
 */
fun Double.convertCurrency(
    fromCurrency: String,
    toCurrency: String,
    exchangeRate: Double = Constants.EUR_TO_USD_RATE
): Double {
    return when {
        fromCurrency == toCurrency -> this
        fromCurrency == "EUR" && toCurrency == "USD" -> this * exchangeRate
        fromCurrency == "USD" && toCurrency == "EUR" -> this / exchangeRate
        else -> this // No conversion if currencies not supported
    }
}

/**
 * Get color based on positive/negative value
 */
fun Double.getAmountColor(): Color {
    return if (this >= 0) Color(0xFF28a745) else Color(0xFFdc3545)
}

/**
 * Check if amount is significant (not zero or very small)
 */
fun Double.isSignificant(threshold: Double = 0.01): Boolean {
    return abs(this) >= threshold
}

/**
 * List extensions
 */

/**
 * Get safe element at index or null
 */
fun <T> List<T>.safeGet(index: Int): T? {
    return if (index in 0 until size) this[index] else null
}

/**
 * Calculate sum of doubles in list
 */
fun List<Double>.sumSafe(): Double {
    return this.fold(0.0) { acc, value -> acc + value }
}

/**
 * Get percentage distribution of list values
 */
fun List<Double>.toPercentageDistribution(): List<Float> {
    val total = this.sumSafe()
    return if (total > 0) {
        this.map { (it / total * 100).toFloat() }
    } else {
        this.map { 0f }
    }
}

/**
 * Composable extensions
 */

/**
 * Conditional modifier
 */
@Composable
fun androidx.compose.ui.Modifier.conditional(
    condition: Boolean,
    modifier: androidx.compose.ui.Modifier.() -> androidx.compose.ui.Modifier
): androidx.compose.ui.Modifier {
    return if (condition) {
        then(modifier())
    } else {
        this
    }
}
