package com.budgettracker.features.financialgoals.presentation.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.budgettracker.core.domain.model.RothIRA
import com.budgettracker.core.domain.model.ContributionFrequency
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditIRADialog(
    ira: RothIRA,
    onDismiss: () -> Unit,
    onConfirm: (RothIRA) -> Unit
) {
    var brokerageName by rememberSaveable { mutableStateOf(ira.brokerageName) }
    var annualLimit by rememberSaveable { mutableStateOf(ira.annualContributionLimit.toString()) }
    var recurringAmount by rememberSaveable { mutableStateOf(ira.recurringContributionAmount?.toString() ?: "") }
    var recurringFrequency by remember { mutableStateOf(ira.recurringContributionFrequency) }
    var showFrequencyDropdown by remember { mutableStateOf(false) }
    
    val frequencies = listOf(
        ContributionFrequency.WEEKLY to "Weekly",
        ContributionFrequency.BIWEEKLY to "Bi-Weekly",
        ContributionFrequency.MONTHLY to "Monthly",
        ContributionFrequency.QUARTERLY to "Quarterly"
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF667eea),
                                Color(0xFF764ba2)
                            )
                        )
                    )
                    .padding(28.dp)
            ) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    // Header with icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Edit Roth IRA",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Brokerage Name
                    OutlinedTextField(
                        value = brokerageName,
                        onValueChange = { brokerageName = it },
                        label = { Text("Brokerage Name *", color = Color.White.copy(alpha = 0.8f)) },
                        placeholder = { Text("e.g., Vanguard, Fidelity", color = Color.White.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                            cursorColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Annual Contribution Limit
                    OutlinedTextField(
                        value = annualLimit,
                        onValueChange = { annualLimit = it },
                        label = { Text("Annual Contribution Limit *", color = Color.White.copy(alpha = 0.8f)) },
                        placeholder = { Text("7000", color = Color.White.copy(alpha = 0.5f)) },
                        prefix = { Text("$", color = Color.White) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                            cursorColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Recurring Contribution Section
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.15f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Repeat,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Recurring Contribution",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Recurring Amount
                            OutlinedTextField(
                                value = recurringAmount,
                                onValueChange = { recurringAmount = it },
                                label = { Text("Amount (Optional)", color = Color.White.copy(alpha = 0.8f)) },
                                placeholder = { Text("0.00", color = Color.White.copy(alpha = 0.5f)) },
                                prefix = { Text("$", color = Color.White) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color.White,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                                    cursorColor = Color.White,
                                    focusedLabelColor = Color.White,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.8f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Frequency Dropdown
                            Box {
                                OutlinedTextField(
                                    value = recurringFrequency.displayName,
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text("Frequency", color = Color.White.copy(alpha = 0.8f)) },
                                    trailingIcon = {
                                        IconButton(onClick = { showFrequencyDropdown = true }) {
                                            Icon(
                                                Icons.Default.ArrowDropDown,
                                                contentDescription = "Select Frequency",
                                                tint = Color.White
                                            )
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color.White,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.6f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                DropdownMenu(
                                    expanded = showFrequencyDropdown,
                                    onDismissRequest = { showFrequencyDropdown = false },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                ) {
                                    frequencies.forEach { (freq, label) ->
                                        DropdownMenuItem(
                                            text = { Text(label) },
                                            onClick = {
                                                recurringFrequency = freq
                                                showFrequencyDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Action buttons
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Update button - Full width
                        Button(
                            onClick = {
                                val updatedIRA = ira.copy(
                                    brokerageName = brokerageName.trim(),
                                    annualContributionLimit = annualLimit.toDoubleOrNull() ?: 7000.0,
                                    recurringContributionAmount = recurringAmount.toDoubleOrNull(),
                                    recurringContributionFrequency = if (recurringAmount.isNotBlank()) recurringFrequency else ContributionFrequency.NONE,
                                    updatedAt = Date()
                                )
                                onConfirm(updatedIRA)
                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = brokerageName.isNotBlank() && annualLimit.toDoubleOrNull() != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF667eea),
                                disabledContainerColor = Color.White.copy(alpha = 0.3f),
                                disabledContentColor = Color.White.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Update IRA",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1
                                )
                            }
                        }
                        
                        // Cancel button - Outlined style
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Cancel",
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

