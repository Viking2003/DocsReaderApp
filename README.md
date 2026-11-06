# Docs Reader Application

A modern, feature-rich Android documentation reader built with Kotlin and Jetpack components. This application provides an immersive reading experience with interactive quizzes, theme customization, and comprehensive learning content about Java Native Interface (JNI) and Android development best practices and Flutter Development Guide.

![Android](https://img.shields.io/badge/Android-34%2B-green)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blue)
![Jetpack](https://img.shields.io/badge/Jetpack-Modern-purple)
![License](https://img.shields.io/badge/License-MIT-yellow)

## 📥 Download APK

**Latest Release**: [DocsReader](../../releases/download/v2.0/DocsReader_v2.0.apk) (v2.0)

### Download Methods
- **GitHub Releases**: Check the [Releases](../../releases) page for all versions.

### System Requirements
- Android 14 (API 34) or higher
- 50 MB of free storage space

> **Note**: For detailed deployment instructions, see [DEPLOYMENT.md](DEPLOYMENT.md)

## ✨ Features

### 📖 Documentation Reader
- **Triple Content Tracks**: Explore comprehensive documentation on:
  - JNI (Java Native Interface) - 5 chapters covering native code integration
  - Android Development Rules - 5 essential architecture and best practice rules
  - Flutter Development - 5 chapters on modern cross-platform development
- **Smooth Navigation**: Swipe between pages with ViewPager2
- **Table of Contents**: Quick-jump to any chapter or section
- **Search Functionality**: Full-text search with real-time highlighting
- **Voice Search**: Use voice commands to search content
- **Text-to-Speech**: Listen to content with built-in TTS
- **Reading Progress**: Automatically track and resume where you left off

### 🎨 Reading Experience
- **Three Reading Themes**:
  - 📜 **Sepia Mode** - Warm, comfortable for daytime reading (default 9 AM - 7 PM)
  - ☀️ **Light Mode** - Clean, bright interface (7 AM - 9 AM)
  - 🌙 **Night Mode** - Dark theme for evening use (7 PM - 7 AM)
- **Auto Theme Switching**: Automatically adjusts theme based on time of day
- **Adjustable Font Size**: Scale text from 12sp to 24sp for optimal readability
- **Kindle-inspired Design**: Professional reading aesthetics

### 🧠 Interactive Learning
- **Comprehensive Quiz System**: 20+ questions across multiple difficulty levels
- **Chapter-Specific Quizzes**: Filter questions by current reading topic
- **Instant Feedback**: Detailed explanations for each answer
- **Progress Tracking**: Monitor quiz completion and scores

### 📊 Statistics & Analytics
- **Reading Time Tracker**: Monitor total time spent reading
- **Quiz Performance**: Track quiz completion rate and average scores
- **Bookmark Management**: Save and revisit important pages
- **Personal Notes**: Take and review notes on any page

### 💾 Data Persistence
- **Jetpack DataStore**: Modern, type-safe data storage
- **Automatic State Saving**: Preserves reading progress, bookmarks, and notes
- **Process Death Recovery**: Survives system reclamation and background termination

### 🏆 Gamification & Achievements
- **XP System**: Earn experience points for reading, quizzes, and more
- **Level Progression**: Level up as you learn
- **13 Unique Achievements**: Unlock badges for various accomplishments
- **Daily Streaks**: Track consecutive days of learning
- **Progress Tracking**: Visual progress bars for each achievement

### ⚙️ Settings & Customization
- **Theme Scheduling**: Customize automatic theme switching times
- **Manual Theme Selection**: Choose between Light, Sepia, and Dark modes
- **Export Data**: Export bookmarks and notes to JSON, CSV, or Markdown
- **Cloud Sync Ready**: Infrastructure for future cloud synchronization

## 🏗️ Architecture

This application follows modern Android development principles:

```
┌─────────────────────────────────────────┐
│           MainActivity                  │
│  (Tab Navigation & Theme Management)   │
└──────────────┬──────────────────────────┘
               │
       ┌───────┴───────┬────────────┬────────────┬────────────┐
       │               │            │            │            │
┌──────▼──────┐ ┌─────▼─────┐ ┌────▼────┐ ┌────▼────┐ ┌────▼────┐
│   Reader    │ │   Quiz    │ │  Stats  │ │Achieve-  │ │Settings │
│  Fragment   │ │ Fragment  │ │Fragment │ │ments     │ │Fragment │
└──────┬──────┘ └─────┬─────┘ └────┬────┘ └────┬────┘ └────┬────┘
       │              │            │            │            │
┌──────▼──────┐ ┌─────▼─────┐       │      ┌─────▼─────┐  │
│  ViewPager2 │ │  Quiz     │       │      │Achievement│  │
│  + Adapter  │ │ Repository│       │      │  Manager  │  │
└──────┬──────┘ └─────┬─────┘       │      └───────────┘  │
       │              │            │            │            │
┌──────▼──────┐ ┌─────▼─────┐       │      ┌─────▼─────┐  │
│  Doc        │ │  Doc      │       │      │Export     │  │
│ Repository  │ │ Quiz Repo │       │      │Manager    │  │
└─────────────┘ └───────────┘       │      └───────────┘  │
                                     │                     │
                            ┌────────▼─────────────────────┘
                            │
                     ┌──────▼──────┐
                     │ Preferences │
                     │  Manager    │
                     └─────────────┘
└──────┬──────┘ └───────────┘       │      └───────────┘
       │                            │
┌──────▼──────┐                     │
│  Doc        │                     │
│ Repository  │                     │
└─────────────┘                     │
                                    │
                         ┌──────────▼──────────┐
                         │  Jetpack DataStore  │
                         │  (Preferences)      │
                         └─────────────────────┘
```

### Key Architectural Patterns
- **Fragment-based Navigation**: TabLayout with fragment switching
- **Repository Pattern**: Centralized data management for documents and quizzes
- **StateFlow/LiveData**: Reactive state management
- **Coroutines**: Asynchronous operations for I/O and background tasks
- **Single Source of Truth**: DataStore as the persistent data layer

## 🛠️ Tech Stack

- **Language**: Kotlin
- **Minimum SDK**: API 34 (Android 14)
- **Target SDK**: API 37 (Android 15)
- **Build System**: Gradle with Kotlin DSL

### Dependencies
- **Jetpack Core**:
  - `androidx.core:core-ktx` - Core Kotlin extensions
  - `androidx.appcompat:appcompat` - AppCompat library
  - `com.google.android.material:material` - Material Design components
  - `androidx.lifecycle:lifecycle-runtime-ktx` - Lifecycle-aware components

- **Data Persistence**:
  - `androidx.datastore:datastore-preferences` - Modern key-value storage

- **UI Components**:
  - `androidx.games:games-activity` - Enhanced activity support
  - ViewPager2 - Smooth page swiping
  - TabLayout - Navigation tabs

- **Testing**:
  - JUnit 4 - Unit testing
  - AndroidX Test Runner - Instrumented testing
  - Espresso - UI testing

## 📱 Screenshots
<img width="364" height="743" alt="image" src="https://github.com/user-attachments/assets/2af7f51f-b950-447c-9607-6368a10d48f0" />
<img width="368" height="735" alt="image" src="https://github.com/user-attachments/assets/dec66d67-69fc-4008-b034-c0f591887df4" />
<img width="364" height="732" alt="image" src="https://github.com/user-attachments/assets/af7d3e78-3a11-4d0e-80d8-0b6dddd1a090" />
<img width="366" height="745" alt="image" src="https://github.com/user-attachments/assets/9ec4b5dd-128b-42da-9c48-8889df6fc57e" />
<img width="369" height="749" alt="image" src="https://github.com/user-attachments/assets/43fc68e3-9a12-4d23-95dd-59eb1487a420" />

### Reader View
- Clean, distraction-free reading interface
- Page navigation with progress indicator
- Quick access to bookmarks, table of contents, and notes

### Quiz Mode
- Multiple-choice questions with instant feedback
- Difficulty-based categorization
- Chapter-specific filtering

### Statistics Dashboard
- Visual cards displaying reading metrics
- Quiz performance tracking
- Bookmark and note summaries

## 🚀 Getting Started

### Prerequisites
- Android Studio Koala or later
- Android SDK API 34+
- JDK 11 or higher

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/Viking2003/DocsReaderApp.git
   cd DocsReaderApp
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory

3. **Sync Gradle**
   - Android Studio will automatically prompt to sync Gradle
   - Wait for dependencies to download

4. **Build and Run**
   - Connect an Android device or start an emulator
   - Click the Run button (▶️) or press `Shift + F10`

### Building from Command Line

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Build and copy to apks folder (Windows)
build-apk.bat release

# Build and copy to apks folder (Linux/Mac)
./build-apk.sh release

# Run tests
./gradlew test
./gradlew connectedAndroidTest
```

### Building for Distribution

To build an APK for distribution:

1. **Windows**: Run `build-apk.bat release`
2. **Linux/Mac**: Run `./build-apk.sh release`

The APK will be automatically copied to the `apks/` folder, which is tracked in Git for easy distribution.

## 📚 Content Overview

### JNI Documentation
1. **Chapter 1: Overview of JNI** - Understanding the Java Native Interface
2. **Chapter 2: Native Method Signatures** - Function name mapping and parameter marshalling
3. **Chapter 3: JNI Types & String Conversion** - Type mapping and string lifecycle management
4. **Chapter 4: JNI Local & Global References** - Memory management in native code
5. **Chapter 5: Threading & Exception Safety** - Multi-threading and error handling

### Android Development Rules
1. **Rule 1: Modern Android Architecture** - MVVM, Unidirectional Data Flow, and layering
2. **Rule 2: Component Lifecycles & Leaks** - Lifecycle states and memory leak prevention
3. **Rule 3: Performance & Coroutines** - Main thread safety and coroutine dispatchers
4. **Rule 4: Android Security & Intents** - Component export rules and secure storage
5. **Rule 5: UI/UX & Accessibility** - Touch targets, edge-to-edge, and contrast guidelines

### Flutter Development
1. **Chapter 1: Flutter Architecture Overview** - Widget tree, build method & rendering pipeline
2. **Chapter 2: State Management in Flutter** - setState, Provider, Riverpod & Bloc patterns
3. **Chapter 3: Flutter Navigation & Routing** - Navigator 2.0, named routes & Auto Route
4. **Chapter 4: Flutter Animations** - Implicit, explicit & Hero animations
5. **Chapter 5: Flutter Platform Integration** - Platform channels, method channels & plugins

## 🎯 Usage Guide

### Reading
1. **Navigate Content**: Use tabs to switch between JNI and Android Rules documentation
2. **Page Navigation**: Swipe left/right or use arrow buttons to navigate pages
3. **Search**: Enter keywords in the search bar to find content across all pages
4. **Table of Contents**: Tap the TOC button to jump to specific chapters

### Customization
1. **Theme Switching**: Tap the theme button to cycle through Sepia → Light → Dark modes
2. **Font Size**: Use the +/- buttons to adjust text size (12sp - 24sp)

### Learning
1. **Take Quizzes**: Tap the Quick Quiz button to start a quiz on the current topic
2. **Bookmark Pages**: Tap the bookmark icon to save important pages
3. **Add Notes**: Tap the notes button to write personal notes on any page

### Statistics
1. **View Progress**: Switch to the Stats tab to see reading time, quiz scores, and more
2. **Track Growth**: Monitor your learning journey over time

## 🧪 Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests "com.example.myfirstandroidapplication.data.DocRepositoryTest"
```

## 📝 Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/myfirstandroidapplication/
│   │   │   ├── MainActivity.kt              # Main activity with navigation
│   │   │   ├── ReaderFragment.kt            # Documentation reader
│   │   │   ├── QuizFragment.kt              # Quiz interface
│   │   │   ├── StatsFragment.kt            # Statistics dashboard
│   │   │   ├── adapters/                    # UI adapters
│   │   │   │   ├── ReaderAdapter.kt
│   │   │   │   └── ReaderTheme.kt
│   │   │   ├── data/                        # Data layer
│   │   │   │   ├── DocRepository.kt         # Document content
│   │   │   │   ├── QuizRepository.kt        # Quiz questions
│   │   │   │   └── PreferencesManager.kt    # DataStore wrapper
│   │   │   ├── models/                      # Data models
│   │   │   │   ├── DocPage.kt
│   │   │   │   └── QuizQuestion.kt
│   │   │   └── dialogs/                     # Dialog components
│   │   │       ├── NotesDialog.kt
│   │   │       ├── SearchDialog.kt
│   │   │       └── TableOfContentsDialog.kt
│   │   ├── res/                             # Resources
│   │   │   ├── layout/                      # XML layouts
│   │   │   ├── values/                      # Strings, colors, themes
│   │   │   └── drawable/                    # Drawables and icons
│   │   └── AndroidManifest.xml
├── build.gradle.kts                         # App-level build config
└── proguard-rules.pro                       # ProGuard rules
```

## 🔮 Future Enhancements

- [ ] Additional documentation topics
- [ ] Voice search capability
- [ ] Text-to-speech for content
- [ ] Achievements and gamification
- [ ] Dark mode scheduling customization

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Vivek Yadav**

- GitHub: [Viking2003](https://github.com/Viking2003)
- LinkedIn: [Vivek Yadav](https://linkedin.com/in/vivek-yadav-805052312)

## 📞 Support

For support, please open an issue in the GitHub repository.

---

Built with ❤️ using Kotlin and Jetpack
