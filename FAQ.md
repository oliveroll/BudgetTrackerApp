# Budget Tracker - FAQ & Troubleshooting

## 🤔 Frequently Asked Questions

### General Questions

**Q: Is this app specifically for international students?**
A: Yes! Budget Tracker is designed specifically for international students on OPT, H1B, and other visas. It includes visa-specific expense categories, budget templates optimized for student finances, and features that help navigate US financial systems.

**Q: Does the app work offline?**
A: Absolutely! The app follows an offline-first approach. You can add transactions, create budgets, and view analytics without an internet connection. Data syncs automatically when you're connected.

**Q: Is my financial data secure?**
A: Yes. We use bank-level encryption, Firebase security, and local biometric authentication. Your data is stored securely and only accessible by you.

**Q: Can I export my data?**
A: Yes, you can export your transactions and budgets to CSV format for tax purposes or personal records.

### OPT/Visa Specific Questions

**Q: How does the app help with OPT compliance?**
A: The app tracks your income to help ensure you stay within OPT regulations. It also has specific categories for visa-related expenses and helps plan for visa transitions.

**Q: What visa-specific categories are available?**
A: We include categories for H1B application fees, visa renewal costs, immigration lawyer fees, and other visa-related expenses that regular budget apps don't track.

**Q: Can I track emergency fund for visa situations?**
A: Yes! The app includes emergency fund planning specifically designed for visa holders who may face unexpected immigration-related expenses or job transitions.

### Budget Templates

**Q: What is the "OPT Student Budget" template?**
A: This template is optimized for international students with:
- Higher emergency fund allocation (6+ months)
- Visa expense planning
- Conservative spending recommendations
- Focus on building credit and savings

**Q: How does the 50/30/20 rule work?**
A: 50% for needs (rent, groceries, utilities), 30% for wants (entertainment, dining out), and 20% for savings and debt payments.

**Q: Can I customize budget templates?**
A: Yes! You can modify any template or create your own custom budget allocations.

### Technical Questions

**Q: Which Android versions are supported?**
A: The app supports Android 7.0 (API level 24) and above, covering 95%+ of active Android devices.

**Q: How does cloud sync work?**
A: Data syncs automatically with Firebase when connected to internet. All data is encrypted and tied to your account.

**Q: Can I use the app on multiple devices?**
A: Yes! Sign in with the same account on multiple devices and your data will sync automatically.

## 🔧 Troubleshooting

### App Issues

**Problem: App crashes on startup**
```
Solution:
1. Clear app cache: Settings > Apps > Budget Tracker > Storage > Clear Cache
2. Restart your device
3. Ensure you have Android 7.0 or higher
4. Update to latest app version
5. If persists, reinstall the app
```

**Problem: Transactions not syncing**
```
Solution:
1. Check internet connection
2. Verify you're logged in: Settings > Account
3. Force sync: Pull down on transaction list
4. Check Firebase connectivity in Settings > Data & Sync
5. Log out and log back in
```

**Problem: Biometric authentication not working**
```
Solution:
1. Ensure biometric is set up in device settings
2. Re-register fingerprint/face in device settings
3. Toggle biometric setting in app: Settings > Security
4. Clear app data and re-setup
```

**Problem: Budget calculations seem wrong**
```
Solution:
1. Check transaction date ranges
2. Verify transaction categories are correct
3. Ensure recurring transactions are set properly
4. Check for duplicate transactions
5. Recalculate budget: Budget > Menu > Recalculate
```

### Firebase/Sync Issues

**Problem: "Authentication failed" error**
```
Solution:
1. Check internet connection
2. Sign out and sign back in
3. Clear app cache
4. Update app to latest version
5. Contact support if persists
```

**Problem: Old data appearing after reinstall**
```
Solution:
This is normal - your data is safely stored in the cloud.
1. Wait for full sync to complete
2. Check Settings > Data & Sync for sync status
3. Pull to refresh on main screens
```

### Performance Issues

**Problem: App is slow or laggy**
```
Solution:
1. Restart the app
2. Clear app cache
3. Free up device storage (need 1GB+ free)
4. Close other apps running in background
5. Restart device
6. Update app to latest version
```

**Problem: High battery usage**
```
Solution:
1. Disable auto-sync: Settings > Data & Sync > Auto-sync OFF
2. Reduce sync frequency
3. Turn off notifications if not needed
4. Update to latest app version (includes performance improvements)
```

### Data Issues

**Problem: Missing transactions**
```
Solution:
1. Check date filter settings
2. Look in "All Categories" view
3. Check if accidentally deleted (Check Trash/Recently Deleted)
4. Verify you're logged into correct account
5. Force sync: Pull down to refresh
```

**Problem: Duplicate transactions**
```
Solution:
1. This can happen during sync conflicts
2. Delete duplicates manually
3. Check recurring transaction settings
4. Report to support if frequent occurrence
```

**Problem: Cannot delete transaction**
```
Solution:
1. Check if it's a recurring transaction (delete series vs single)
2. Ensure you have internet connection for sync
3. Try force-closing and reopening app
4. Clear app cache
```

### Account Issues

**Problem: Forgot password**
```
Solution:
1. Use "Forgot Password" on login screen
2. Check email (including spam folder)
3. Follow reset instructions
4. Contact support if email not received
```

**Problem: Cannot create account**
```
Solution:
1. Check internet connection
2. Verify email format is correct
3. Use different email if current one has issues
4. Try again later (temporary server issues)
5. Contact support
```

### Export/Import Issues

**Problem: Export not working**
```
Solution:
1. Ensure sufficient storage space
2. Grant file permissions to app
3. Try smaller date range
4. Check Downloads folder
5. Use different export format (CSV vs PDF)
```

**Problem: Cannot import bank statements**
```
Solution:
1. Ensure file is in supported format (CSV, PDF)
2. Check file isn't corrupted
3. Try smaller file size
4. Verify bank statement format is recognized
5. Manual entry as alternative
```

## 📞 Getting Help

### Self-Help Resources
1. **In-app Help**: Settings > Help & Support
2. **Tutorial**: First-time user onboarding
3. **Tips**: Look for 💡 icons throughout the app

### Contact Support
- **Email**: support@budgettracker.app
- **Response Time**: 24-48 hours
- **Include**: App version, device model, Android version, detailed description

### Feature Requests
- **GitHub Issues**: For developers and technical users
- **In-app Feedback**: Settings > Send Feedback
- **User Forum**: Community discussions and tips

### Emergency Issues
For account access or data loss:
- **Priority Support**: support-urgent@budgettracker.app
- **Include**: Account email, approximate last backup date

## 🔍 Common Error Codes

| Error Code | Meaning | Solution |
|------------|---------|----------|
| AUTH_001 | Authentication failed | Re-login to account |
| SYNC_002 | Sync conflict | Force refresh data |
| DB_003 | Database error | Clear app cache |
| NET_004 | Network timeout | Check internet connection |
| FILE_005 | Export failed | Check storage permissions |
| BUDGET_006 | Budget calculation error | Recalculate budget |

## 📱 Device Compatibility

### Minimum Requirements
- **Android**: 7.0 (API level 24)
- **RAM**: 2GB
- **Storage**: 100MB free space
- **Internet**: Required for sync (optional for core features)

### Recommended Specifications
- **Android**: 10.0 or higher
- **RAM**: 4GB+
- **Storage**: 1GB+ free space
- **Biometric**: Fingerprint or face unlock supported

### Known Issues
- **Huawei devices**: May need to enable auto-start for background sync
- **Samsung devices**: Disable battery optimization for best performance
- **Xiaomi devices**: Allow notifications in Security app

## 🆕 What's New

Check the in-app changelog (Settings > About > What's New) for latest features and fixes.

Remember: Budget Tracker is designed to help international students succeed financially in the US. We're constantly improving based on user feedback!
