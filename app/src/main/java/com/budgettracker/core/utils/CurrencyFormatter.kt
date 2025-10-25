package com.budgettracker.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.budgettracker.features.settings.presentation.SettingsViewModel
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Composable that provides currency formatting based on user settings
 */
@Composable
fun rememberCurrencyFormatter(
    viewModel: SettingsViewModel = hiltViewModel()
): CurrencyFormatter {
    val settings by viewModel.userSettings.collectAsState()
    val currencyCode = settings?.currency ?: "USD"
    val currencySymbol = settings?.currencySymbol ?: "$"
    
    return CurrencyFormatter(currencyCode, currencySymbol)
}

/**
 * Currency formatter that uses user's selected currency
 */
class CurrencyFormatter(
    private val currencyCode: String = "USD",
    private val currencySymbol: String = "$"
) {
    private val numberFormat: NumberFormat by lazy {
        try {
            val locale = getLocaleForCurrency(currencyCode)
            val format = NumberFormat.getCurrencyInstance(locale)
            format.currency = Currency.getInstance(currencyCode)
            format
        } catch (e: Exception) {
            // Fallback to USD if currency code is invalid
            NumberFormat.getCurrencyInstance(Locale.US)
        }
    }
    
    /**
     * Format amount as currency string
     */
    fun format(amount: Double): String {
        return try {
            numberFormat.format(amount)
        } catch (e: Exception) {
            // Fallback formatting
            "$currencySymbol${String.format("%.2f", amount)}"
        }
    }
    
    /**
     * Format amount as currency string with sign
     */
    fun formatWithSign(amount: Double, isIncome: Boolean): String {
        val formatted = format(amount)
        return if (isIncome) "+$formatted" else "-$formatted"
    }
    
    /**
     * Get just the currency symbol
     */
    fun getSymbol(): String = currencySymbol
    
    /**
     * Get the currency code
     */
    fun getCode(): String = currencyCode
    
    companion object {
        /**
         * Get appropriate locale for a currency code
         */
        private fun getLocaleForCurrency(currencyCode: String): Locale {
            return when (currencyCode) {
                "USD" -> Locale.US
                "EUR" -> Locale.GERMANY
                "GBP" -> Locale.UK
                "JPY" -> Locale.JAPAN
                "CAD" -> Locale.CANADA
                "AUD" -> Locale("en", "AU")
                "CHF" -> Locale("de", "CH")
                "CNY" -> Locale.CHINA
                "INR" -> Locale("en", "IN")
                "MXN" -> Locale("es", "MX")
                else -> Locale.US
            }
        }
        
        /**
         * Create formatter with specific currency (for non-composable contexts)
         */
        fun create(currencyCode: String = "USD", currencySymbol: String = "$"): CurrencyFormatter {
            return CurrencyFormatter(currencyCode, currencySymbol)
        }
    }
}

