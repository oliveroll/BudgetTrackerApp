# Contributing to Budget Tracker Android App

## 🎯 Project Overview

This Budget Tracker Android app is specifically designed for international students on OPT/H1B visas. We welcome contributions that enhance financial management for this community.

## 🏗️ Architecture Guidelines

### Clean Architecture Layers
1. **Presentation Layer**: Jetpack Compose UI + ViewModels
2. **Domain Layer**: Business logic, use cases, entities  
3. **Data Layer**: Room (local) + Firebase (cloud sync)

### MVVM Pattern
- ViewModels manage UI state with StateFlow/LiveData
- UI observes ViewModels reactively
- Business logic stays in use cases

## 🛠️ Development Setup

### Prerequisites
- Android Studio Arctic Fox or later
- Kotlin 1.9+
- Android SDK 24+ (target 34)
- Firebase project access

### Getting Started
1. Fork the repository
2. Clone your fork: `git clone https://github.com/yourusername/BudgetTrackerApp.git`
3. Set up Firebase configuration
4. Add `google-services.json` to `app/` directory
5. Update `local.properties` with your Android SDK path

### Code Style
- Follow Kotlin coding conventions
- Use Material Design 3 components
- Implement proper error handling
- Add comprehensive comments for complex logic

## 📱 Feature Areas

### Core Features
- **Transaction Management**: 37+ categories including OPT/visa specific
- **Budget Planning**: Templates (50/30/20, Zero-based, OPT Student)
- **Financial Goals**: Emergency fund, retirement planning
- **Analytics**: Spending trends and category breakdowns

### OPT/Visa Specific Features
- H1B application expense tracking
- Visa fee management
- Income compliance monitoring
- Immigration-related financial planning

## 🎨 UI Guidelines

### Jetpack Compose
- Use Material Design 3 theming
- Implement responsive layouts
- Follow accessibility guidelines
- Support dark/light themes

### Navigation
- Bottom navigation for main features
- Navigation Compose for screen transitions
- Deep linking support

## 🗃️ Data Guidelines

### Domain Models
```kotlin
// Example: Transaction model structure
data class Transaction(
    val id: String,
    val userId: String,
    val amount: Double,
    val category: TransactionCategory,
    val type: TransactionType,
    val date: Date,
    val isRecurring: Boolean,
    // ... other properties
)
```

### Database
- Room for local storage
- Firebase Firestore for cloud sync
- Offline-first approach
- Proper type converters for complex data

## 🧪 Testing

### Test Structure
```
src/test/           # Unit tests
src/androidTest/    # Integration tests
```

### Testing Guidelines
- Unit tests for ViewModels and use cases
- Integration tests for database operations
- UI tests for critical user flows
- Mock external dependencies

## 🔄 Git Workflow

### Branch Naming
- `feature/transaction-categories`
- `bugfix/budget-calculation`
- `enhancement/opt-features`

### Commit Messages
```
feat: add H1B expense tracking category
fix: resolve budget template calculation error
docs: update API documentation
```

### Pull Request Process
1. Create feature branch from `main`
2. Implement changes with tests
3. Update documentation if needed
4. Submit PR with clear description
5. Address review feedback
6. Squash and merge

## 📋 Issue Guidelines

### Bug Reports
- Clear reproduction steps
- Expected vs actual behavior
- Device/OS information
- Screenshots if applicable

### Feature Requests
- Problem statement
- Proposed solution
- Impact on OPT/visa users
- Implementation considerations

## 🎯 Priority Areas

### High Priority
1. **OPT/Visa Features**: Immigration-specific functionality
2. **Core Functionality**: Transaction and budget management
3. **Data Integrity**: Reliable sync and backup
4. **User Experience**: Intuitive, accessible design

### Medium Priority
1. **Analytics**: Advanced reporting and insights
2. **Automation**: Smart categorization and recurring transactions
3. **Integration**: Bank import and external services
4. **Performance**: Optimization and efficiency

## 📚 Resources

### Documentation
- [Android Architecture Components](https://developer.android.com/topic/architecture)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)
- [Firebase Documentation](https://firebase.google.com/docs)

### Project Specific
- Domain models: `core/domain/model/`
- Constants: `core/utils/Constants.kt`
- Architecture overview: `PROJECT_MDC.md`

## 🤝 Code of Conduct

### Our Commitment
- Inclusive environment for international students
- Respectful collaboration
- Focus on user needs and financial empowerment
- Professional and constructive feedback

### Unacceptable Behavior
- Discrimination or harassment
- Disrespectful language or conduct
- Spam or off-topic discussions
- Violation of privacy or security

## 📞 Getting Help

### Questions?
- Open a discussion for general questions
- Create an issue for bugs or feature requests
- Check existing documentation first

### Contact
- GitHub Issues: Technical problems
- GitHub Discussions: General questions
- Email: For security or privacy concerns

Thank you for contributing to a tool that helps international students achieve financial stability! 🚀

## 📚 External Development Resources

### Android Development Learning
- **Android Developer Fundamentals**: https://developer.android.com/courses/fundamentals-training/overview-v2
- **Kotlin for Android Developers**: https://developer.android.com/kotlin/get-started
- **Jetpack Compose Tutorial**: https://developer.android.com/jetpack/compose/tutorial
- **Android Architecture Codelabs**: https://developer.android.com/codelabs#architecture

### Best Practices & Guidelines
- **Android API Guidelines**: https://developer.android.com/guide
- **Kotlin Coding Conventions**: https://kotlinlang.org/docs/coding-conventions.html
- **Material Design Guidelines**: https://m3.material.io/foundations
- **Android Performance Best Practices**: https://developer.android.com/topic/performance

### Firebase Development
- **Firebase Codelabs**: https://firebase.google.com/codelabs
- **Firestore Best Practices**: https://firebase.google.com/docs/firestore/best-practices
- **Firebase Security**: https://firebase.google.com/docs/rules
- **Firebase Performance**: https://firebase.google.com/docs/perf-mon/get-started-android

### Testing & Quality Assurance
- **Android Testing Codelabs**: https://developer.android.com/codelabs#testing
- **Test-Driven Development**: https://developer.android.com/training/testing/fundamentals
- **UI Testing with Espresso**: https://developer.android.com/training/testing/espresso
- **Code Coverage**: https://developer.android.com/studio/test/code-coverage

### Open Source Contribution
- **GitHub Flow**: https://guides.github.com/introduction/flow/
- **Git Best Practices**: https://www.atlassian.com/git/tutorials/comparing-workflows
- **Code Review Best Practices**: https://google.github.io/eng-practices/review/
- **Semantic Versioning**: https://semver.org/

### Accessibility & Internationalization
- **Android Accessibility**: https://developer.android.com/guide/topics/ui/accessibility
- **Internationalization**: https://developer.android.com/guide/topics/resources/localization
- **RTL Support**: https://developer.android.com/training/basics/supporting-devices/languages
- **Material Design Accessibility**: https://m3.material.io/foundations/accessible-design/overview
