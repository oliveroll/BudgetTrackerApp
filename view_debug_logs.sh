#!/bin/bash
# Debug log viewer for Budget Tracker Fixed Expense issue
# This script filters logcat to show only relevant debug output

echo "=========================================="
echo "Budget Tracker - Live Debug Monitor"
echo "=========================================="
echo ""
echo "Watching for:"
echo "  🔹 Budget screen edits (BudgetRepoDebug)"
echo "  🔹 ViewModel updates (BudgetViewModelDebug)"
echo "  🔹 Dashboard calculations (DashboardDebug)"
echo ""
echo "Press Ctrl+C to stop"
echo "=========================================="
echo ""

adb logcat -c  # Clear existing logs
adb logcat -s DashboardDebug:D BudgetViewModelDebug:D BudgetRepoDebug:D

