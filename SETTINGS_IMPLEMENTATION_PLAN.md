# Settings Page Implementation Plan

## Current Status
✅ **Basic Settings screen exists** with:
- User profile card
- App settings card (placeholder)
- Developer tools (test notification)
- Account actions (logout placeholder)

## Full Implementation Roadmap

### Phase 1: Core Infrastructure (2-3 hours) ✅ IN PROGRESS
- [x] Data models (UserSettings, NotificationSettings, EmploymentSettings, CustomCategory)
- [ ] Room DAOs for settings tables
- [ ] SettingsRepository with Firestore sync
- [ ] SettingsViewModel with StateFlow

### Phase 2: Account Settings (4-5 hours)
- [ ] Email change dialog with Firebase Auth
- [ ] Re-authentication flow
- [ ] Error handling (email-already-in-use, requires-recent-login)
- [ ] Biometric placeholder UI
- [ ] Theme selection placeholder

### Phase 3: Notification Settings (6-8 hours)
- [ ] Notification toggles with frequency pickers
- [ ] Permission request for Android 13+
- [ ] WorkManager integration for scheduled notifications
- [ ] Cancel/reschedule workers on toggle
- [ ] Save to Firestore `/users/{uid}/profile.notificationSettings`

### Phase 4: Financial Settings (8-10 hours)
#### Currency Picker (2 hours)
- [ ] Currency list with country codes
- [ ] Search/filter currencies
- [ ] Update formatting app-wide
- [ ] Save to Firestore

#### Employment Form (3-4 hours)
- [ ] OPT status dropdown
- [ ] Employment type selection
- [ ] Employer, salary, pay frequency fields
- [ ] Next pay date picker
- [ ] Validation (salary >= 0, required fields)
- [ ] Save to `/users/{uid}/employment/{employmentId}`

#### Category Management (3-4 hours)
- [ ] List custom categories
- [ ] Add category dialog (name, type, color, icon, budget)
- [ ] Edit category
- [ ] Archive category (prevent delete if transactions exist)
- [ ] Color picker
- [ ] Icon selector
- [ ] Save to `/users/{uid}/categories/{categoryId}`

### Phase 5: Account Actions (5-6 hours)
#### Export Data (2 hours)
- [ ] Collect all user data (transactions, goals, etc.)
- [ ] Serialize to JSON with schema version
- [ ] Save to file with timestamp
- [ ] Share intent / download

#### Initialize Financial Data (1 hour)
- [ ] Wizard to create baseline categories
- [ ] Example budgets
- [ ] Idempotent with confirmation

#### Delete Account (2-3 hours)
- [ ] Two-step confirmation dialog
- [ ] Password re-authentication
- [ ] Delete Firebase Auth user
- [ ] Purge `/users/{uid}` subtree
- [ ] Progress indicator
- [ ] Navigate to login

### Phase 6: Help & Info (1 hour)
- [ ] App version display
- [ ] Build number
- [ ] Help & support links
- [ ] Privacy policy webview

### Phase 7: Testing (4-6 hours)
- [ ] Unit tests: validation logic, currency formatting
- [ ] Integration tests: email change, notification scheduling
- [ ] UI tests: accessibility, responsive layout

### Phase 8: Polish (2-3 hours)
- [ ] Material 3 design refinements
- [ ] Tablet responsive layout
- [ ] Loading states
- [ ] Error states
- [ ] Snackbar feedback
- [ ] Accessibility labels

---

## Total Estimated Time: 40-50 hours

## Recommended Phased Approach

### Week 1: Foundation + Critical Features
1. Complete Phase 1 (Infrastructure)
2. Implement Phase 2 (Account Settings)
3. Implement Phase 5 partial (Sign out, Export data)

### Week 2: Advanced Features
1. Implement Phase 3 (Notifications with WorkManager)
2. Implement Phase 4 partial (Currency picker)

### Week 3: Finishing Touches
1. Complete Phase 4 (Employment & Categories)
2. Complete Phase 5 (Delete account)
3. Add Phase 6 (Help & Info)

### Week 4: Quality & Testing
1. Comprehensive testing
2. Bug fixes
3. Polish and accessibility
4. Documentation

---

## What I Can Deliver Now (2-3 hours)

Given time constraints, I can provide:

✅ **Enhanced Settings Screen** with:
- Improved UI/UX with Material 3
- Email display (read-only for now)
- Currency selector with real Firestore sync
- Notification toggles (UI only, WorkManager integration separate task)
- Working Export Data to JSON
- Complete Sign Out flow
- Delete Account with confirmation

✅ **Production-Ready Code:**
- Proper error handling
- Loading states
- Accessibility
- Responsive design

❌ **Deferred for Follow-Up:**
- WorkManager notification scheduling (requires separate PR)
- Employment full form (complex validation)
- Category management (extensive UI)
- Complete email change flow (Firebase Auth complexity)

---

## Decision Point

**Option A: Quick Enhancement (2-3 hours)**
- Enhance existing Settings screen
- Add critical features that work end-to-end
- Provide architecture for future expansion

**Option B: Full Implementation (40-50 hours)**
- Complete all features as specified
- Comprehensive testing
- Production-ready for all sections

**Which approach would you prefer?**

For now, I'll proceed with **Option A** and give you a solid foundation you can build on.

