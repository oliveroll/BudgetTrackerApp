package com.budgettracker.features.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.budgettracker.features.settings.data.models.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailChangeDialog(
    currentEmail: String,
    onDismiss: () -> Unit,
    onConfirm: (password: String, newEmail: String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var confirmEmail by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Email") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Current email: $currentEmail",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = newEmail,
                    onValueChange = { 
                        newEmail = it
                        error = null
                    },
                    label = { Text("New Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null
                )
                
                OutlinedTextField(
                    value = confirmEmail,
                    onValueChange = { 
                        confirmEmail = it
                        error = null
                    },
                    label = { Text("Confirm New Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null
                )
                
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        password.isEmpty() -> error = "Password is required"
                        newEmail.isEmpty() -> error = "New email is required"
                        newEmail != confirmEmail -> error = "Emails don't match"
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches() -> 
                            error = "Invalid email format"
                        else -> {
                            onConfirm(password, newEmail)
                        }
                    }
                }
            ) {
                Text("Change Email")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmploymentDialog(
    employment: EmploymentSettings?,
    onDismiss: () -> Unit,
    onSave: (EmploymentSettings) -> Unit
) {
    var optStatus by remember { mutableStateOf(employment?.optStatus ?: "") }
    var employmentType by remember { mutableStateOf(employment?.employmentType ?: "FULL_TIME") }
    var employer by remember { mutableStateOf(employment?.employer ?: "") }
    var jobTitle by remember { mutableStateOf(employment?.jobTitle ?: "") }
    var salary by remember { mutableStateOf(employment?.annualSalary?.toString() ?: "") }
    var payFrequency by remember { mutableStateOf(employment?.payFrequency ?: PayFrequency.BI_WEEKLY) }
    
    var showEmploymentTypeMenu by remember { mutableStateOf(false) }
    var showPayFrequencyMenu by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Employment Details") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // OPT Status
                OutlinedTextField(
                    value = optStatus,
                    onValueChange = { optStatus = it },
                    label = { Text("OPT/Visa Status (Optional)") },
                    placeholder = { Text("e.g., OPT, H1B, Green Card") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Employment Type
                Box {
                    OutlinedTextField(
                        value = employmentType.replace("_", " "),
                        onValueChange = {},
                        label = { Text("Employment Type") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showEmploymentTypeMenu = true }) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        }
                    )
                    
                    DropdownMenu(
                        expanded = showEmploymentTypeMenu,
                        onDismissRequest = { showEmploymentTypeMenu = false }
                    ) {
                        listOf("FULL_TIME", "PART_TIME", "CONTRACT", "UNEMPLOYED").forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.replace("_", " ")) },
                                onClick = {
                                    employmentType = type
                                    showEmploymentTypeMenu = false
                                }
                            )
                        }
                    }
                }
                
                // Employer
                OutlinedTextField(
                    value = employer,
                    onValueChange = { employer = it },
                    label = { Text("Employer") },
                    placeholder = { Text("Company name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Job Title
                OutlinedTextField(
                    value = jobTitle,
                    onValueChange = { jobTitle = it },
                    label = { Text("Job Title (Optional)") },
                    placeholder = { Text("e.g., Software Engineer") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Annual Salary
                OutlinedTextField(
                    value = salary,
                    onValueChange = { 
                        salary = it.filter { char -> char.isDigit() || char == '.' }
                        error = null
                    },
                    label = { Text("Annual Salary") },
                    placeholder = { Text("80000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = { Text("$") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null
                )
                
                // Pay Frequency
                Box {
                    OutlinedTextField(
                        value = payFrequency.name.replace("_", " "),
                        onValueChange = {},
                        label = { Text("Pay Frequency") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showPayFrequencyMenu = true }) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        }
                    )
                    
                    DropdownMenu(
                        expanded = showPayFrequencyMenu,
                        onDismissRequest = { showPayFrequencyMenu = false }
                    ) {
                        PayFrequency.values().forEach { freq ->
                            DropdownMenuItem(
                                text = { Text(freq.name.replace("_", " ")) },
                                onClick = {
                                    payFrequency = freq
                                    showPayFrequencyMenu = false
                                }
                            )
                        }
                    }
                }
                
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val salaryValue = salary.toDoubleOrNull()
                    when {
                        employmentType != "UNEMPLOYED" && employer.isEmpty() -> 
                            error = "Employer is required"
                        salaryValue == null || salaryValue < 0 -> 
                            error = "Invalid salary"
                        else -> {
                            onSave(
                                (employment ?: EmploymentSettings(
                                    id = UUID.randomUUID().toString(),
                                    userId = "" // Will be set by repository
                                )).copy(
                                    optStatus = optStatus.ifEmpty { null },
                                    employmentType = employmentType,
                                    employer = employer.ifEmpty { null },
                                    jobTitle = jobTitle.ifEmpty { null },
                                    annualSalary = salaryValue,
                                    payFrequency = payFrequency,
                                    updatedAt = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDialog(
    category: CustomCategory?,
    onDismiss: () -> Unit,
    onSave: (CustomCategory) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var type by remember { mutableStateOf(category?.type ?: CategoryType.EXPENSE) }
    var colorHex by remember { mutableStateOf(category?.colorHex ?: "#FF6B6B") }
    var budgetAmount by remember { mutableStateOf(category?.budgetedAmount?.toString() ?: "") }
    
    var showTypeMenu by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) "Add Category" else "Edit Category") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        error = null
                    },
                    label = { Text("Category Name") },
                    placeholder = { Text("e.g., Coffee") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null
                )
                
                // Type
                Box {
                    OutlinedTextField(
                        value = type.name,
                        onValueChange = {},
                        label = { Text("Type") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showTypeMenu = true }) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        }
                    )
                    
                    DropdownMenu(
                        expanded = showTypeMenu,
                        onDismissRequest = { showTypeMenu = false }
                    ) {
                        CategoryType.values().forEach { catType ->
                            DropdownMenuItem(
                                text = { Text(catType.name) },
                                onClick = {
                                    type = catType
                                    showTypeMenu = false
                                }
                            )
                        }
                    }
                }
                
                // Color Picker (Simple preset colors)
                Text("Color", style = MaterialTheme.typography.labelMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A",
                        "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E2"
                    ).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(color)))
                                .clickable { colorHex = color }
                                .then(
                                    if (colorHex == color) {
                                        Modifier.border(
                                            3.dp,
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape
                                        )
                                    } else Modifier
                                )
                        )
                    }
                }
                
                // Budget Amount
                OutlinedTextField(
                    value = budgetAmount,
                    onValueChange = { 
                        budgetAmount = it.filter { char -> char.isDigit() || char == '.' }
                    },
                    label = { Text("Monthly Budget (Optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = { Text("$") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        name.isEmpty() -> error = "Name is required"
                        else -> {
                            onSave(
                                (category ?: CustomCategory(
                                    id = UUID.randomUUID().toString(),
                                    userId = "", // Will be set by repository
                                    name = name,
                                    type = type
                                )).copy(
                                    name = name,
                                    type = type,
                                    colorHex = colorHex,
                                    budgetedAmount = budgetAmount.toDoubleOrNull(),
                                    updatedAt = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (password: String) -> Unit,
    onConfirmGoogle: () -> Unit,
    isGoogleUser: Boolean
) {
    var password by remember { mutableStateOf("") }
    var confirmText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
        },
        title = { Text("Delete Account") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "This action is IRREVERSIBLE!",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                
                Text(
                    "Your account and ALL data will be permanently deleted:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    listOf(
                        "All transactions",
                        "All financial goals",
                        "All subscriptions and bills",
                        "All categories",
                        "Employment history",
                        "Settings and preferences"
                    ).forEach { item ->
                        Text(
                            "• $item",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                OutlinedTextField(
                    value = confirmText,
                    onValueChange = { 
                        confirmText = it
                        error = null
                    },
                    label = { Text("Type DELETE to confirm") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null
                )
                
                // Only show password field for email/password users
                if (!isGoogleUser) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            error = null
                        },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        isError = error != null
                    )
                } else {
                    // Info for Google users
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "You signed in with Google. You'll be asked to sign in with Google again to confirm account deletion.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        confirmText != "DELETE" -> error = "Please type DELETE to confirm"
                        !isGoogleUser && password.isEmpty() -> error = "Password is required"
                        else -> {
                            if (isGoogleUser) {
                                onConfirmGoogle()
                            } else {
                                onConfirm(password)
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(if (isGoogleUser) "Continue with Google" else "Delete Account")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyPickerDialog(
    currentCurrency: String,
    onDismiss: () -> Unit,
    onSelect: (code: String, symbol: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    // Common currencies
    val currencies = remember {
        listOf(
            Triple("USD", "$", "United States Dollar"),
            Triple("EUR", "€", "Euro"),
            Triple("GBP", "£", "British Pound"),
            Triple("JPY", "¥", "Japanese Yen"),
            Triple("CAD", "C$", "Canadian Dollar"),
            Triple("AUD", "A$", "Australian Dollar"),
            Triple("CHF", "Fr", "Swiss Franc"),
            Triple("CNY", "¥", "Chinese Yuan"),
            Triple("INR", "₹", "Indian Rupee"),
            Triple("MXN", "$", "Mexican Peso")
        )
    }
    
    val filteredCurrencies = currencies.filter {
        searchQuery.isEmpty() || 
        it.first.contains(searchQuery, ignoreCase = true) ||
        it.third.contains(searchQuery, ignoreCase = true)
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Currency") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search currency...") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, null) }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(filteredCurrencies) { (code, symbol, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(code, symbol) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                symbol,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.width(48.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(code, fontWeight = FontWeight.Bold)
                                Text(
                                    name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (code == currentCurrency) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

