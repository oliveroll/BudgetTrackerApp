package com.budgettracker.core.utils

import android.content.Context
import android.net.Uri
import com.budgettracker.core.domain.model.Transaction
import com.budgettracker.core.domain.model.TransactionCategory
import com.budgettracker.core.domain.model.TransactionType
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * Fixed Regions Bank PDF Parser specifically for DEPOSITS & CREDITS section
 * Based on the exact format from Statement_September.pdf
 */
class FixedRegionsBankPDFParser(private val context: Context) {
    
    init {
        PDFBoxResourceLoader.init(context)
    }
    
    /**
     * Parse Regions Bank PDF statement and extract transactions
     */
    suspend fun parseRegionsBankStatement(uri: Uri): Result<List<Transaction>> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("Could not open PDF file"))
            
            val document = PDDocument.load(inputStream)
            val stripper = PDFTextStripper()
            val text = stripper.getText(document)
            document.close()
            inputStream.close()
            
            android.util.Log.d("FixedRegionsParser", "Extracted text length: ${text.length}")
            
            val transactions = extractAllTransactions(text)
            Result.success(transactions)
            
        } catch (e: Exception) {
            android.util.Log.e("FixedRegionsParser", "Error parsing PDF: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Extract all transactions from Regions Bank statement
     */
    private fun extractAllTransactions(text: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val lines = text.split("\n")
        
        android.util.Log.d("FixedRegionsParser", "Processing ${lines.size} lines")
        
        var isInDepositsSection = false
        var isInWithdrawalsSection = false
        
        for ((lineIndex, line) in lines.withIndex()) {
            val cleanLine = line.trim()
            
            // Section detection
            when {
                cleanLine.contains("DEPOSITS & CREDITS", ignoreCase = true) ||
                cleanLine.contains("Deposits & Credits", ignoreCase = true) -> {
                    isInDepositsSection = true
                    isInWithdrawalsSection = false
                    android.util.Log.d("FixedRegionsParser", "🔍 Found DEPOSITS & CREDITS section at line $lineIndex")
                    continue
                }
                cleanLine.contains("WITHDRAWALS", ignoreCase = true) ||
                (cleanLine.contains("Withdrawals", ignoreCase = true) && !cleanLine.contains("&")) -> {
                    isInDepositsSection = false
                    isInWithdrawalsSection = true
                    android.util.Log.d("FixedRegionsParser", "🔍 Found WITHDRAWALS section at line $lineIndex")
                    continue
                }
                cleanLine.startsWith("Total") && (isInDepositsSection || isInWithdrawalsSection) -> {
                    // End of section
                    android.util.Log.d("FixedRegionsParser", "🔍 End of section at line $lineIndex: $cleanLine")
                    isInDepositsSection = false
                    isInWithdrawalsSection = false
                    continue
                }
            }
            
            // Skip empty lines and headers
            if (cleanLine.isEmpty() || cleanLine.length < 8) continue
            
            // Parse transactions based on section
            if (isInDepositsSection) {
                parseDepositTransaction(cleanLine, lineIndex)?.let { 
                    transactions.add(it)
                    android.util.Log.d("FixedRegionsParser", "✅ DEPOSIT: ${it.description} - $${it.amount}")
                }
            } else if (isInWithdrawalsSection) {
                parseWithdrawalTransaction(cleanLine, lineIndex)?.let { 
                    transactions.add(it)
                    android.util.Log.d("FixedRegionsParser", "✅ WITHDRAWAL: ${it.description} - $${it.amount}")
                }
            }
        }
        
        android.util.Log.d("FixedRegionsParser", "Total transactions found: ${transactions.size}")
        return transactions.distinctBy { "${it.description.take(30)}_${it.amount}_${it.date.time / 86400000}" }
    }
    
    /**
     * Parse deposit transaction from DEPOSITS & CREDITS section
     * Format: "08/26    Oliver Ollesch  Payments Oliver Ollesch 281475400133133                    90.93"
     */
    private fun parseDepositTransaction(line: String, lineIndex: Int): Transaction? {
        return try {
            // Pattern for deposits: Date + Description + Amount at end
            val pattern = Pattern.compile("(\\d{2}/\\d{2})\\s+(.+?)\\s+(\\d+(?:,\\d+)*\\.\\d{2})\\s*$")
            val matcher = pattern.matcher(line)
            
            if (matcher.find()) {
                val dateStr = matcher.group(1) ?: return null
                val description = matcher.group(2)?.trim() ?: return null
                val amountStr = matcher.group(3) ?: return null
                
                val amount = amountStr.replace(",", "").toDoubleOrNull() ?: return null
                val date = parseDate(dateStr)
                
                android.util.Log.d("FixedRegionsParser", "📈 Parsing deposit: $dateStr | $description | $amountStr")
                
                // Clean up description
                val cleanDescription = cleanDescription(description)
                
                Transaction(
                    id = UUID.randomUUID().toString(),
                    userId = "regions_parsed",
                    amount = amount,
                    category = categorizeDeposit(cleanDescription, amount),
                    type = TransactionType.INCOME,
                    description = cleanDescription,
                    date = date,
                    notes = "Parsed from DEPOSITS & CREDITS section"
                )
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("FixedRegionsParser", "Error parsing deposit line $lineIndex: ${e.message}")
            null
        }
    }
    
    /**
     * Parse withdrawal transaction from WITHDRAWALS section
     */
    private fun parseWithdrawalTransaction(line: String, lineIndex: Int): Transaction? {
        return try {
            // Pattern for withdrawals: Date + Type + Description + Amount
            val patterns = listOf(
                Pattern.compile("(\\d{2}/\\d{2})\\s+(Card\\s+Credit|Card\\s+Purchase|Recurring\\s+Card)\\s+(.+?)\\s+(\\d+(?:,\\d+)*\\.\\d{2})\\s*$"),
                Pattern.compile("(\\d{2}/\\d{2})\\s+(Monthly\\s+Fee|ATM\\s+Fee|Service\\s+Fee)\\s+(\\d+(?:,\\d+)*\\.\\d{2})\\s*$"),
                Pattern.compile("(\\d{2}/\\d{2})\\s+(.+?)\\s+(\\d+(?:,\\d+)*\\.\\d{2})\\s*$")
            )
            
            for ((patternIndex, pattern) in patterns.withIndex()) {
                val matcher = pattern.matcher(line)
                if (matcher.find()) {
                    val dateStr = matcher.group(1) ?: continue
                    val amount: Double
                    val description: String
                    val transactionType: String
                    
                    when (patternIndex) {
                        0 -> {
                            transactionType = matcher.group(2) ?: ""
                            description = matcher.group(3) ?: ""
                            amount = matcher.group(4)?.replace(",", "")?.toDoubleOrNull() ?: continue
                        }
                        1 -> {
                            transactionType = matcher.group(2) ?: ""
                            description = transactionType
                            amount = matcher.group(3)?.replace(",", "")?.toDoubleOrNull() ?: continue
                        }
                        2 -> {
                            transactionType = ""
                            description = matcher.group(2) ?: ""
                            amount = matcher.group(3)?.replace(",", "")?.toDoubleOrNull() ?: continue
                        }
                        else -> continue
                    }
                    
                    val date = parseDate(dateStr)
                    val cleanDescription = cleanDescription("$transactionType $description".trim())
                    
                    android.util.Log.d("FixedRegionsParser", "📉 Parsing withdrawal: $dateStr | $cleanDescription | $amount")
                    
                    return Transaction(
                        id = UUID.randomUUID().toString(),
                        userId = "regions_parsed",
                        amount = amount,
                        category = categorizeExpense(cleanDescription, amount),
                        type = TransactionType.EXPENSE,
                        description = cleanDescription,
                        date = date,
                        notes = "Parsed from WITHDRAWALS section"
                    )
                }
            }
            
            null
        } catch (e: Exception) {
            android.util.Log.e("FixedRegionsParser", "Error parsing withdrawal line $lineIndex: ${e.message}")
            null
        }
    }
    
    /**
     * Parse date from MM/dd format
     */
    private fun parseDate(dateStr: String): Date {
        return try {
            val format = SimpleDateFormat("MM/dd", Locale.US)
            val parsed = format.parse(dateStr)
            if (parsed != null) {
                val calendar = Calendar.getInstance()
                calendar.time = parsed
                calendar.set(Calendar.YEAR, 2025) // Use 2025 for your statement
                calendar.time
            } else {
                Date()
            }
        } catch (e: Exception) {
            Date()
        }
    }
    
    /**
     * Clean description text
     */
    private fun cleanDescription(description: String): String {
        return description
            .trim()
            .replace(Regex("\\s+"), " ")
            .replace(Regex("\\d{10,}"), "") // Remove long account numbers
            .trim()
            .take(100)
    }
    
    /**
     * Categorize deposit transactions
     */
    private fun categorizeDeposit(description: String, amount: Double): TransactionCategory {
        val desc = description.lowercase()
        
        return when {
            desc.contains("gusto") && amount > 1000 -> TransactionCategory.SALARY
            desc.contains("oliver ollesch payments") -> TransactionCategory.OTHER_INCOME
            desc.contains("wise") -> TransactionCategory.OTHER_INCOME
            desc.contains("venmo") -> TransactionCategory.OTHER_INCOME
            desc.contains("actverify") -> TransactionCategory.OTHER_INCOME
            amount > 1000 -> TransactionCategory.SALARY
            else -> TransactionCategory.OTHER_INCOME
        }
    }
    
    /**
     * Categorize expense transactions
     */
    private fun categorizeExpense(description: String, amount: Double): TransactionCategory {
        val desc = description.lowercase()
        
        return when {
            desc.contains("amazon") -> TransactionCategory.MISCELLANEOUS
            desc.contains("mcdonald") || desc.contains("starbucks") -> TransactionCategory.DINING_OUT
            desc.contains("walmart") -> TransactionCategory.GROCERIES
            desc.contains("gas") || desc.contains("shell") -> TransactionCategory.TRANSPORTATION
            desc.contains("monthly fee") -> TransactionCategory.MISCELLANEOUS
            else -> TransactionCategory.MISCELLANEOUS
        }
    }
}
