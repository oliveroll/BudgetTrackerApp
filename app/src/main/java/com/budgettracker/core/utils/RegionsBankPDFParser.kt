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
 * Specialized PDF Parser for Regions Bank statements
 * Based on the actual format from Statement_September.pdf
 */
class RegionsBankPDFParser(private val context: Context) {
    
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
            
            android.util.Log.d("RegionsBankParser", "Extracted text length: ${text.length}")
            
            val transactions = extractRegionsTransactions(text)
            Result.success(transactions)
            
        } catch (e: Exception) {
            android.util.Log.e("RegionsBankParser", "Error parsing PDF: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Extract transactions specifically from Regions Bank statement format
     */
    private fun extractRegionsTransactions(text: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val lines = text.split("\n")
        
        android.util.Log.d("RegionsBankParser", "Processing ${lines.size} lines")
        
        // Regions Bank transaction patterns based on your actual statement:
        // Format: "07/21 Card Credit  Venmo*ollesch O  4829  New York City Ny 10014    5595 104.49"
        // Format: "08/14 Monthly Fee 8.00"
        
        val patterns = listOf(
            // Pattern 1: Date Transaction_Type Description ... Amount
            Pattern.compile("(\\d{2}/\\d{2})\\s+(Card\\s+Credit|Card\\s+Purchase|Recurring\\s+Card\\s+Transaction)\\s+(.+?)\\s+(\\d+\\.\\d{2})$"),
            
            // Pattern 2: Date Fee_Type Amount
            Pattern.compile("(\\d{2}/\\d{2})\\s+(Monthly\\s+Fee|Service\\s+Fee|ATM\\s+Fee)\\s+(\\d+\\.\\d{2})$"),
            
            // Pattern 3: General date ... amount at end
            Pattern.compile("(\\d{2}/\\d{2})\\s+(.+?)\\s+(\\d+\\.\\d{2})$"),
            
            // Pattern 4: Date with full year format
            Pattern.compile("(\\d{2}/\\d{2}/\\d{4})\\s+(.+?)\\s+(\\d+\\.\\d{2})$")
        )
        
        for ((lineIndex, line) in lines.withIndex()) {
            val cleanLine = line.trim()
            
            // Skip short lines or headers
            if (cleanLine.length < 15) continue
            
            // Skip lines that are clearly not transactions
            if (cleanLine.startsWith("ACCOUNT") || 
                cleanLine.startsWith("Page") ||
                cleanLine.startsWith("LIFEGREEN") ||
                cleanLine.startsWith("Beginning") ||
                cleanLine.startsWith("Ending") ||
                cleanLine.contains("Balance") ||
                cleanLine.startsWith("SUMMARY") ||
                cleanLine.startsWith("Deposits") ||
                cleanLine.startsWith("Withdrawals") ||
                cleanLine.startsWith("Fees") ||
                cleanLine.startsWith("Automatic")) {
                continue
            }
            
            android.util.Log.d("RegionsBankParser", "Checking line $lineIndex: $cleanLine")
            
            for ((patternIndex, pattern) in patterns.withIndex()) {
                val matcher = pattern.matcher(cleanLine)
                if (matcher.find()) {
                    try {
                        val transaction = parseRegionsTransaction(matcher, cleanLine, lineIndex, patternIndex)
                        if (transaction != null) {
                            transactions.add(transaction)
                            android.util.Log.d("RegionsBankParser", "✅ Parsed: ${transaction.description} - $${transaction.amount}")
                            break
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("RegionsBankParser", "Error parsing line $lineIndex: ${e.message}")
                    }
                }
            }
        }
        
        android.util.Log.d("RegionsBankParser", "Total transactions found: ${transactions.size}")
        
        return transactions.distinctBy { "${it.description.take(20)}_${it.amount}_${it.date.time / 86400000}" }
    }
    
    /**
     * Parse a specific Regions Bank transaction
     */
    private fun parseRegionsTransaction(
        matcher: java.util.regex.Matcher, 
        line: String, 
        lineIndex: Int,
        patternIndex: Int
    ): Transaction? {
        return try {
            val dateStr = matcher.group(1) ?: return null
            val amountStr: String
            val description: String
            val transactionType: String
            
            when (patternIndex) {
                0 -> {
                    // Pattern 1: Date Transaction_Type Description ... Amount
                    transactionType = matcher.group(2) ?: ""
                    description = matcher.group(3) ?: ""
                    amountStr = matcher.group(4) ?: return null
                    
                    android.util.Log.d("RegionsBankParser", "Pattern 1: date=$dateStr, type=$transactionType, desc=$description, amount=$amountStr")
                }
                1 -> {
                    // Pattern 2: Date Fee_Type Amount
                    transactionType = matcher.group(2) ?: ""
                    description = transactionType
                    amountStr = matcher.group(3) ?: return null
                    
                    android.util.Log.d("RegionsBankParser", "Pattern 2: date=$dateStr, fee=$transactionType, amount=$amountStr")
                }
                2 -> {
                    // Pattern 3: General format
                    description = matcher.group(2) ?: ""
                    amountStr = matcher.group(3) ?: return null
                    transactionType = ""
                    
                    android.util.Log.d("RegionsBankParser", "Pattern 3: date=$dateStr, desc=$description, amount=$amountStr")
                }
                3 -> {
                    // Pattern 4: Full date format
                    description = matcher.group(2) ?: ""
                    amountStr = matcher.group(3) ?: return null
                    transactionType = ""
                    
                    android.util.Log.d("RegionsBankParser", "Pattern 4: date=$dateStr, desc=$description, amount=$amountStr")
                }
                else -> return null
            }
            
            // Parse amount
            val amount = amountStr.replace(",", "").toDoubleOrNull() ?: return null
            
            if (amount <= 0) {
                android.util.Log.d("RegionsBankParser", "Invalid amount: $amount")
                return null
            }
            
            // Parse date (MM/dd format, assume current year)
            val date = parseRegionsDate(dateStr)
            
            // Clean up description
            val cleanDescription = cleanRegionsDescription(description, transactionType)
            
            // Determine transaction type
            val type = if (transactionType.contains("Credit", ignoreCase = true) || 
                          cleanDescription.uppercase().contains("DEPOSIT")) {
                TransactionType.INCOME
            } else {
                TransactionType.EXPENSE
            }
            
            // Categorize transaction
            val category = categorizeRegionsTransaction(cleanDescription, amount)
            
            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                userId = "regions_parsed",
                amount = amount,
                category = category,
                type = type,
                description = cleanDescription,
                date = date,
                notes = "Parsed from Regions Bank statement"
            )
            
            android.util.Log.d("RegionsBankParser", "✅ Created: ${transaction.description} - $${transaction.amount} (${transaction.type})")
            
            transaction
            
        } catch (e: Exception) {
            android.util.Log.e("RegionsBankParser", "Error creating transaction: ${e.message}")
            null
        }
    }
    
    /**
     * Parse Regions Bank date format (MM/dd or MM/dd/yyyy) with proper year handling
     */
    private fun parseRegionsDate(dateStr: String): Date {
        return try {
            // Try different date formats
            val formats = listOf(
                SimpleDateFormat("MM/dd/yyyy", Locale.US),
                SimpleDateFormat("MM/dd", Locale.US)
            )
            
            for (format in formats) {
                try {
                    val parsed = format.parse(dateStr)
                    if (parsed != null) {
                        val calendar = Calendar.getInstance()
                        calendar.time = parsed
                        
                        // For MM/dd format, assign year intelligently
                        if (dateStr.length <= 5) {
                            val currentCalendar = Calendar.getInstance()
                            val currentMonth = currentCalendar.get(Calendar.MONTH)
                            val currentYear = currentCalendar.get(Calendar.YEAR)
                            val parsedMonth = calendar.get(Calendar.MONTH)
                            
                            // If parsed month is in the future compared to current month,
                            // assume it's from the previous year
                            if (parsedMonth > currentMonth + 3) {
                                calendar.set(Calendar.YEAR, currentYear - 1)
                            } else {
                                calendar.set(Calendar.YEAR, currentYear)
                            }
                        }
                        
                        android.util.Log.d("RegionsBankParser", "Parsed date: $dateStr -> ${SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)}")
                        return calendar.time
                    }
                } catch (e: Exception) {
                    // Continue to next format
                    continue
                }
            }
            
            // Fallback to current date
            android.util.Log.w("RegionsBankParser", "Could not parse date: $dateStr, using current date")
            Date()
            
        } catch (e: Exception) {
            android.util.Log.e("RegionsBankParser", "Error parsing date $dateStr: ${e.message}")
            Date()
        }
    }
    
    /**
     * Clean up Regions Bank description
     */
    private fun cleanRegionsDescription(description: String, transactionType: String): String {
        var clean = description.trim()
        
        // Remove location codes and extra data
        clean = clean.replace(Regex("\\s+\\d{4}\\s+.*"), "")
        clean = clean.replace(Regex("\\s+\\d{5}$"), "")
        
        // Add transaction type prefix if not already included
        if (transactionType.isNotEmpty() && !clean.contains(transactionType, ignoreCase = true)) {
            clean = "$transactionType - $clean"
        }
        
        return clean.take(100)
    }
    
    /**
     * Categorize Regions Bank transactions
     */
    private fun categorizeRegionsTransaction(description: String, amount: Double): TransactionCategory {
        val desc = description.lowercase()
        
        return when {
            // Income
            desc.contains("venmo") && desc.contains("credit") -> TransactionCategory.OTHER_INCOME
            desc.contains("payroll") || desc.contains("salary") -> TransactionCategory.SALARY
            desc.contains("deposit") && amount > 1000 -> TransactionCategory.SALARY
            
            // Fees
            desc.contains("monthly fee") || desc.contains("service fee") -> TransactionCategory.MISCELLANEOUS
            
            // Shopping
            desc.contains("amazon") -> TransactionCategory.MISCELLANEOUS
            desc.contains("walmart") || desc.contains("target") -> TransactionCategory.GROCERIES
            
            // Food
            desc.contains("mcdonald") || desc.contains("starbucks") -> TransactionCategory.DINING_OUT
            desc.contains("restaurant") -> TransactionCategory.DINING_OUT
            
            // Entertainment/Services
            desc.contains("foursqu") || desc.contains("netflix") || desc.contains("spotify") -> TransactionCategory.SUBSCRIPTIONS
            
            // Transportation
            desc.contains("shell") || desc.contains("gas") || desc.contains("fuel") -> TransactionCategory.TRANSPORTATION
            
            // Specialty stores
            desc.contains("wine") || desc.contains("spirit") -> TransactionCategory.GROCERIES
            desc.contains("backfire") || desc.contains("paddle") -> TransactionCategory.MISCELLANEOUS
            desc.contains("tamper") -> TransactionCategory.MISCELLANEOUS
            
            else -> TransactionCategory.MISCELLANEOUS
        }
    }
}
