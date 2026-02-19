# GitHub Diary Guidelines

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
app/src/commonMain/kotlin/io/github/t45k/githubDiary/
├── Main.kt                    # (プラットフォーム別のエントリーポイント)
├── calendar/                  # カレンダー画面 (月間表示、リポジトリ、ViewModel)
├── diary/                     # 日記機能 (DiaryContent, リポジトリ, 編集/プレビュー)
├── monthlyNote/               # 月間目標機能 (GoalContent, リポジトリ, 編集/プレビュー)
├── setting/                   # 設定画面 (リポジトリ, ViewModel, ストレージ)
├── github/                    # GitHub APIクライアント (GitHubClient, モデル)
├── ui/                        # 共通UIコンポーネント、ナビゲーション
│   ├── AppScreen.kt          # ルートComposable (Navigation3による画面遷移)
│   ├── NavRoute.kt           # ナビゲーション定義
│   └── common/               # 共通コンポーネント (エディタ等)
├── util/                      # ユーティリティ (DateProvider, Format等)
└── AppModule.kt               # Koin DI構成
```

## Key Patterns

- **Screen-based ViewModel:** 各画面は `androidx.lifecycle.ViewModel` を継承したViewModelを持ち、`StateFlow` で Compose 状態を管理する
- **Koin DI:** 依存性の注入に Koin を使用。ViewModel は `koinViewModel` で取得する
- **Navigation3:** `AppScreen.kt` で `NavDisplay` と `backStack` (MutableStateList) を使用して画面遷移を管理する
- **Repository pattern:** `GitHubClient` が REST API を叩き、各リポジトリがデータアクセスを抽象化する
- **Arrow Either:** 設定のバリデーションや例外処理で `either` ブロックを使用する

## GitHub API Integration

- **Diary path format:** `{year}/{month}/{day}/README.md` (e.g., `2026/01/02/README.md`)
- **Authentication:** Bearer token via PAT
- **Content encoding:** Base64 for file operations
- **Settings storage:** `~/.github_diary/settings.json`

## Hardcoded Configuration

- Timezone: JST (Asia/Tokyo)
- Theme: Dark only
- Calendar: Sunday-start week
- Diary header: `# YYYY/MM/DD (Day)`
- Monthly note header: `# YYYY/MM`

## Specification

See `spec/spec.md` for full feature specification (Japanese).

## Git Commit Rules

- Junieが生成・関与したコミットには、必ず以下のCo-authored-byトレーラーを含めること:
  ```
  Co-authored-by: junie-agent <247260674+junie-agent@users.noreply.github.com>
  ```
- `git commit --trailer "Co-authored-by: junie-agent <247260674+junie-agent@users.noreply.github.com>"` を使うと簡単に追加できる
