package com.budgettracker.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Helper function to format currency without needing to inject ViewModel
 * Uses default formatter if no currency is set
 */
@Composable
fun rememberSimpleCurrencyFormat(): (Double) -> String {
    val formatter = rememberCurrencyFormatter()
    return remember(formatter) {
        { amount: Double -> formatter.format(amount) }
    }
}

/**
 * Non-composable fallback for currency formatting
 * Uses USD as default
 */
fun formatCurrency(amount: Double, currencyCode: String = "USD", symbol: String = "$"): String {
    return CurrencyFormatter.create(currencyCode, symbol).format(amount)
}

