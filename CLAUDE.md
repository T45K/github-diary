# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

GitHub Diary is a Kotlin/Compose Desktop application that manages diary entries stored as Markdown files in GitHub repositories. Users interact through a calendar interface to view, edit, and preview daily entries.

## Build Commands

```bash
./gradlew build    # Compile and build
./gradlew run      # Run the application
./gradlew test     # Run tests (JUnit 6)
./gradlew clean    # Clean build artifacts
```

## Technology Stack

- **Language:** Kotlin 2.3.0, JVM 25
- **UI:** Jetpack Compose Desktop 1.10.0-rc02
- **HTTP:** Ktor Client 3.3.3
- **Testing:** JUnit Jupiter 6.0.0
- **Functional:** Arrow Core (Either for error handling)

## Architecture

```
app/src/main/kotlin/
├── Main.kt                    # Entry point
├── core/
│   ├── entity/               # Domain models (Calendar, DiaryContent, GitHubPersonalAccessToken)
│   ├── repository/           # Data access (GitHubClient, DiaryRepository, SettingRepository)
│   └── time/                 # DateProvider (JST timezone)
└── ui/
    ├── AppScreen.kt          # Root composable with routing
    ├── AppViewModel.kt       # Global state
    ├── calendar/             # Calendar screen (month view)
    ├── edit/                 # Diary editor
    ├── preview/              # Markdown preview
    ├── settings/             # Token/repo configuration
    └── navigation/           # NavRoute enum
```

## Key Patterns

- **ViewModel per screen:** Each screen has a ViewModel extending `androidx.lifecycle.ViewModel` with `mutableStateOf` for Compose state
- **Repository pattern:** `GitHubClient` handles REST API calls, repositories abstract data access
- **Enum-based routing:** `NavRoute` enum manages navigation via `AppViewModel.currentRoute`
- **Arrow Either:** Used in settings validation for functional error handling

## GitHub API Integration

- **Diary path format:** `{year}/{month}/{day}/README.md` (e.g., `2026/01/02/README.md`)
- **Authentication:** Bearer token via PAT
- **Content encoding:** Base64 for file operations
- **Settings storage:** `~/.github_diary/settings.json`

## Hardcoded Configuration

- Timezone: JST (Asia/Tokyo)
- Theme: Dark only
- Calendar: Sunday-start week
- Diary header: `# YYYY/MM/DD (Day)` format

## Specification

See `spec/spec.md` for full feature specification (Japanese).
