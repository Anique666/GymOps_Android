# Gym Management System

An Android application for managing gym members, membership plans, and dashboard analytics.

## Features

- **Dashboard**: View total members, active members, expired memberships, and expiring memberships at a glance
- **Member Management**: Add, edit, view, and manage gym members
- **Plan Management**: Create and manage membership plans
- **Real-time Updates**: Live tracking of membership status
- **Local Database**: Persistent storage using Room database

## Tech Stack

- **Language**: Kotlin
- **Framework**: Android (minSdk 21, targetSdk 33)
- **Database**: Android Room ORM
- **Architecture**: MVVM with LiveData
- **UI**: Material Design 3 components
- **Build**: Gradle 7.4 with Android Gradle Plugin 7.3.0-beta05

## Requirements

- Android SDK 33+
- Java 11+
- Gradle 7.4
- Android Studio 2022.1+

## Setup

1. Clone the repository
2. Open in Android Studio
3. Ensure JDK 11+ is selected in Android Studio settings
4. Build and run on an emulator or device

```bash
./gradlew assembleDebug
```

## Project Structure

```
app/
├── src/
│   └── main/
│       ├── java/com/example/gymmanagement/
│       │   ├── data/          # Database and repositories
│       │   ├── di/            # Dependency injection
│       │   ├── ui/            # UI activities and viewmodels
│       │   └── ...
│       └── res/               # Resources (layouts, values, etc)
├── build.gradle               # App-level gradle configuration
└── ...
```

## Building

Debug build:
```bash
./gradlew :app:assembleDebug
```

Release build:
```bash
./gradlew :app:assembleRelease
```

## License

This project is licensed under the MIT License.
