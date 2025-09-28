# Changelog

All notable changes to the Budget Tracker Android app will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- GitMCP integration for enhanced AI assistant support
- Comprehensive documentation (API, Architecture, Deployment)
- Contributing guidelines for open source development

### Changed
- Enhanced project documentation for better AI understanding
- Updated README with GitMCP badge and comprehensive feature overview

### Developer
- Added `llms.txt` for AI-optimized documentation
- Created detailed API documentation
- Added architecture overview and deployment guides

## [1.0.0] - 2024-01-15 (Planned Initial Release)

### Added
- **Core Transaction Management**
  - Add, edit, delete transactions with 37+ categories
  - Recurring transaction support
  - Receipt attachment capability
  - Transaction search and filtering
  - Bulk import from CSV/PDF bank statements

- **OPT/Visa Specific Features**
  - H1B application expense tracking
  - Visa fee management categories
  - Employment status tracking (OPT, H1B, etc.)
  - Immigration-related financial planning

- **Budget Planning & Templates**
  - Monthly budget creation and tracking
  - 50/30/20 budget rule template
  - Zero-based budgeting template
  - OPT Student optimized budget template
  - Real-time budget vs actual spending comparison
  - Budget status alerts (on track, near limit, over budget)

- **Financial Analytics**
  - Spending by category breakdown
  - Monthly and yearly trend analysis
  - Budget performance metrics
  - Custom date range reports
  - Visual charts with Vico Charts library

- **Savings Goals & Emergency Fund**
  - Multiple savings goal tracking
  - Emergency fund planning (6+ months for visa holders)
  - Progress visualization and milestones
  - Automatic savings recommendations

- **Security & Privacy**
  - Firebase Authentication integration
  - Biometric login (fingerprint/face)
  - Local data encryption
  - Secure cloud backup with Firebase

- **User Experience**
  - Material Design 3 interface
  - Dark/light theme support
  - Offline-first functionality
  - Real-time data synchronization
  - Intuitive navigation with bottom tabs

### Technical Implementation
- **Architecture**: Clean Architecture with MVVM pattern
- **UI Framework**: Jetpack Compose
- **Database**: Room (local) + Firebase Firestore (cloud)
- **Dependency Injection**: Hilt (Dagger)
- **Networking**: Retrofit + OkHttp
- **Image Loading**: Coil
- **Background Tasks**: WorkManager
- **Charts**: Vico Charts library

### Default Configuration
- Monthly income: $5,470 (typical OPT salary)
- Annual salary: $80,000
- Emergency fund: 6 months expenses
- Default company: "Ixana Quasistatics"
- Default state: Indiana
- 2024 contribution limits: Roth IRA ($7,000), 401K ($23,000)

## [0.9.0] - 2023-12-15 (Beta Release)

### Added
- Beta testing with 50 international students
- Core transaction and budget functionality
- Basic Firebase integration
- Initial UI implementation with Jetpack Compose

### Changed
- Refined user interface based on beta feedback
- Improved performance and stability
- Enhanced error handling and user feedback

### Fixed
- Sync issues with Firebase
- Budget calculation edge cases
- UI responsiveness on different screen sizes

## [0.8.0] - 2023-11-30 (Alpha Release)

### Added
- Alpha version with core features
- Transaction management
- Basic budget creation
- Firebase setup and integration
- Initial user authentication

### Technical
- Project structure established
- Domain models implemented
- Room database setup
- Basic repository pattern implementation

## [0.7.0] - 2023-11-15 (Pre-Alpha)

### Added
- Project initialization
- Architecture planning and setup
- Domain model definitions
- Technology stack selection
- Development environment setup

### Technical Foundation
- Clean Architecture structure
- MVVM pattern implementation
- Jetpack Compose UI framework
- Firebase project configuration
- Hilt dependency injection setup

## Development Milestones

### Phase 1: Foundation (Completed)
- [x] Project structure and architecture
- [x] Domain models (Transaction, Budget, UserProfile)
- [x] Room entities and type converters
- [x] Constants and configuration
- [x] Build system and dependencies

### Phase 2: Core Features (In Progress)
- [ ] Repository layer implementation
- [ ] Use cases and business logic
- [ ] ViewModels and state management
- [ ] Firebase integration
- [ ] Database DAOs and setup

### Phase 3: UI Implementation (Planned)
- [ ] Main navigation structure
- [ ] Transaction screens (list, add, edit)
- [ ] Budget planning and tracking screens
- [ ] Analytics and reporting dashboard
- [ ] User profile and settings

### Phase 4: Advanced Features (Planned)
- [ ] PDF bank statement parsing
- [ ] Recurring transaction automation
- [ ] Advanced analytics and insights
- [ ] Goal tracking and notifications
- [ ] Data export and backup features

### Phase 5: Polish & Launch (Planned)
- [ ] Performance optimization
- [ ] Accessibility improvements
- [ ] Comprehensive testing
- [ ] Play Store optimization
- [ ] Documentation and help system

## Version Naming Convention

- **Major.Minor.Patch** (e.g., 1.2.3)
- **Major**: Breaking changes or significant new features
- **Minor**: New features, backward compatible
- **Patch**: Bug fixes and small improvements

## Release Notes Template

### [Version] - Date

#### Added
- New features and capabilities

#### Changed
- Changes to existing functionality

#### Deprecated
- Features that will be removed in future versions

#### Removed
- Features removed in this version

#### Fixed
- Bug fixes and issues resolved

#### Security
- Security improvements and vulnerability fixes

## Support for International Students

This app is specifically designed to help international students on OPT, H1B, and other visas manage their finances effectively while navigating the complexities of the US financial system. Each release focuses on features that matter most to this community:

- Visa-specific expense tracking
- Emergency fund planning for visa transitions
- Income compliance monitoring for OPT regulations
- Budget templates optimized for student finances
- Financial planning for immigration processes

---

**Note**: This changelog will be updated with each release. For technical details and API changes, see [API.md](API.md) and [ARCHITECTURE.md](ARCHITECTURE.md).
