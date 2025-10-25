package com.budgettracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

/**
 * Modern, unified month switcher component using Material Design 3
 * 
 * Features:
 * - Scrollable month tabs with smooth animations
 * - Left/right navigation arrows
 * - Adaptive color theming
 * - Responsive design for all screen sizes
 * - Touch targets ≥ 48dp
 * 
 * Usage:
 * ```kotlin
 * MonthSwitcher(
 *     selectedMonth = selectedMonth,
 *     selectedYear = selectedYear,
 *     onMonthYearSelected = { month, year -> 
 *         // Update your state
 *     }
 * )
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthSwitcher(
    selectedMonth: Int, // 0-11 (Calendar.JANUARY to Calendar.DECEMBER)
    selectedYear: Int,
    onMonthYearSelected: (month: Int, year: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Generate list of months (12 months before and after current)
    val availableMonths = remember {
        val months = mutableListOf<Pair<Int, Int>>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -12)
        
        repeat(25) { // 12 past + current + 12 future
            months.add(Pair(cal.get(Calendar.MONTH), cal.get(Calendar.YEAR)))
            cal.add(Calendar.MONTH, 1)
        }
        months
    }
    
    val selectedIndex = availableMonths.indexOfFirst { 
        it.first == selectedMonth && it.second == selectedYear 
    }
    
    val scrollableState = rememberScrollableTabRowState(
        initialFirstVisibleTabIndex = (selectedIndex - 1).coerceAtLeast(0)
    )
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Previous Month Button
            IconButton(
                onClick = {
                    if (selectedIndex > 0) {
                        val prev = availableMonths[selectedIndex - 1]
                        onMonthYearSelected(prev.first, prev.second)
                    }
                },
                modifier = Modifier.size(48.dp),
                enabled = selectedIndex > 0
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous Month",
                    tint = if (selectedIndex > 0) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    modifier = Modifier.size(28.dp)
                )
            }
            
            // Scrollable Month Tabs
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                ScrollableTabRow(
                    selectedTabIndex = selectedIndex.coerceAtLeast(0),
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    edgePadding = 8.dp,
                    indicator = { tabPositions ->
                        if (selectedIndex in tabPositions.indices) {
                            val currentTabPosition = tabPositions[selectedIndex]
                            TabRowDefaults.SecondaryIndicator(
                                Modifier
                                    .fillMaxWidth()
                                    .wrapContentSize(Alignment.BottomStart)
                                    .offset(x = currentTabPosition.left)
                                    .width(currentTabPosition.width),
                                color = MaterialTheme.colorScheme.primary,
                                height = 3.dp
                            )
                        }
                    },
                    divider = {}
                ) {
                    availableMonths.forEachIndexed { index, (month, year) ->
                        val isSelected = index == selectedIndex
                        val monthName = SimpleDateFormat("MMM", Locale.getDefault())
                            .format(Calendar.getInstance().apply {
                                set(Calendar.MONTH, month)
                                set(Calendar.YEAR, year)
                            }.time)
                        
                        Tab(
                            selected = isSelected,
                            onClick = { onMonthYearSelected(month, year) },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .padding(horizontal = 4.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = monthName,
                                    fontSize = if (isSelected) 14.sp else 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (year != Calendar.getInstance().get(Calendar.YEAR)) {
                                    Text(
                                        text = "'${year % 100}",
                                        fontSize = 10.sp,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Next Month Button
            IconButton(
                onClick = {
                    if (selectedIndex < availableMonths.size - 1) {
                        val next = availableMonths[selectedIndex + 1]
                        onMonthYearSelected(next.first, next.second)
                    }
                },
                modifier = Modifier.size(48.dp),
                enabled = selectedIndex < availableMonths.size - 1
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next Month",
                    tint = if (selectedIndex < availableMonths.size - 1) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

/**
 * Alternative compact version for smaller screens or when space is limited
 */
@Composable
fun CompactMonthSwitcher(
    selectedMonth: Int,
    selectedYear: Int,
    onMonthYearSelected: (month: Int, year: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val currentDate = remember(selectedMonth, selectedYear) {
        Calendar.getInstance().apply {
            set(Calendar.MONTH, selectedMonth)
            set(Calendar.YEAR, selectedYear)
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Button
            IconButton(
                onClick = {
                    val newCal = currentDate.clone() as Calendar
                    newCal.add(Calendar.MONTH, -1)
                    onMonthYearSelected(
                        newCal.get(Calendar.MONTH),
                        newCal.get(Calendar.YEAR)
                    )
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous Month",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            // Month Display
            Text(
                text = monthFormat.format(currentDate.time),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            // Next Button
            IconButton(
                onClick = {
                    val newCal = currentDate.clone() as Calendar
                    newCal.add(Calendar.MONTH, 1)
                    onMonthYearSelected(
                        newCal.get(Calendar.MONTH),
                        newCal.get(Calendar.YEAR)
                    )
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next Month",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

/**
 * Minimal month switcher for tight spaces
 */
@Composable
fun MinimalMonthSwitcher(
    selectedMonth: Int,
    selectedYear: Int,
    onMonthYearSelected: (month: Int, year: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthFormat = remember { SimpleDateFormat("MMM yyyy", Locale.getDefault()) }
    val currentDate = remember(selectedMonth, selectedYear) {
        Calendar.getInstance().apply {
            set(Calendar.MONTH, selectedMonth)
            set(Calendar.YEAR, selectedYear)
        }
    }
    
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val newCal = currentDate.clone() as Calendar
                    newCal.add(Calendar.MONTH, -1)
                    onMonthYearSelected(
                        newCal.get(Calendar.MONTH),
                        newCal.get(Calendar.YEAR)
                    )
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous Month",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Text(
                text = monthFormat.format(currentDate.time),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            IconButton(
                onClick = {
                    val newCal = currentDate.clone() as Calendar
                    newCal.add(Calendar.MONTH, 1)
                    onMonthYearSelected(
                        newCal.get(Calendar.MONTH),
                        newCal.get(Calendar.YEAR)
                    )
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next Month",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Helper function to get month name from Calendar month constant
 */
private fun getMonthName(month: Int): String {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.MONTH, month)
    return SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.time)
}

/**
 * Helper function to get short month name from Calendar month constant
 */
private fun getShortMonthName(month: Int): String {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.MONTH, month)
    return SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)
}

@Composable
private fun rememberScrollableTabRowState(
    initialFirstVisibleTabIndex: Int = 0
): ScrollableTabRowState {
    return remember { ScrollableTabRowState(initialFirstVisibleTabIndex) }
}

private class ScrollableTabRowState(
    val initialFirstVisibleTabIndex: Int
)

