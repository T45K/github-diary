# 実装計画書

以下のチェックボックスは、1項目あたり変更ファイルが概ね3〜5個（多くても10個以内）に収まる粒度で分割しています。各項目を順に完了させれば、仕様に沿ったMVPを実装できます。

## 基盤・プロジェクト構成
- [x] Gradle & Compose Desktop初期セットアップ（Mac/JVM想定）
  - `settings.gradle.kts`でプロジェクト名設定、`build.gradle.kts`でCompose Desktop, Kotlin, Ktor, kotlinx-datetime, Markdownレンダラ、JSONシリアライザ、JUnit 6 依存を追加
  - `gradle.properties`でJVM/Composeバージョンや警告設定、`local.properties`は使わない方針を明記
  - `app/src/main/kotlin/Main.kt`にCompose Desktopエントリポイントとウィンドウ設定を仮置き

## 共通基盤・設計方針
- [x] 設定・DI・定数の土台整備
  - `app/src/main/kotlin/core/AppConfig.kt`にベース設定（タイムゾーンJST、GitHub APIベースURL、デフォルトブランチ`main`）
  - `app/src/main/kotlin/core/di/ServiceLocator.kt`でシンプルDI（ViewModel／UseCase／Clientの生成管理）
  - `app/src/main/kotlin/core/model/Result.kt`など共通Result/エラー型を定義

- [x] 日付・パスのユーティリティ
  - `app/src/main/kotlin/core/time/DateProvider.kt`で現在日付(JST)供給、`app/src/main/kotlin/core/time/DateFormatter.kt`で`yyyy/MM/dd (ddd)`やパス`yyyy/MM/dd/README.md`生成
  - `app/src/test/kotlin/core/time/DateFormatterTest.kt`でフォーマットと週開始(日曜)のテスト

## データ層（GitHub連携）
- [x] 認証トークン管理
  - `app/src/main/kotlin/data/auth/TokenStore.kt`でトークン保存/取得（今回は平文ファイルorメモリで簡易実装）、`app/src/main/kotlin/data/auth/AuthMode.kt`でPAT/OAuth種別
  - `app/src/main/kotlin/data/auth/TokenValidator.kt`で`GET /user`を叩いて疎通確認（401/403検知）
  - `app/src/test/kotlin/data/auth/TokenValidatorTest.kt`で疎通ロジックのモックテスト

- [x] GitHub APIクライアント（REST, Ktor）
  - `app/src/main/kotlin/data/github/GitHubClient.kt`でKtor HttpClient生成（認証ヘッダ付与、rate-limit対応ログ）
  - `app/src/main/kotlin/data/github/ContentsApi.kt`で`GET /repos/{owner}/{repo}/contents/{path}`と`PUT ...`（Base64, SHA, committer省略でPAT依存）
  - `app/src/main/kotlin/data/github/model/ContentDtos.kt`でDTO/シリアライズ定義
  - `app/src/test/kotlin/data/github/ContentsApiTest.kt`でAPI呼び出しのシリアライズ・パラメータテスト（モックサーバ）

- [x] リポジトリ層
  - `app/src/main/kotlin/data/repo/DiaryRepository.kt`で日記取得/保存/存在確認（409時のハンドリング含む）
  - `app/src/main/kotlin/data/repo/SettingsRepository.kt`でリポ指定`org/repo`保存・検証
  - `app/src/test/kotlin/data/repo/DiaryRepositoryTest.kt`でコンフリクト・未存在時の挙動テスト

## ドメイン・ユースケース
- [x] ユースケース定義
  - `app/src/main/kotlin/domain/usecase/FetchMonthDiariesUseCase.kt`で月単位取得（不足日を判定）
  - `app/src/main/kotlin/domain/usecase/FetchDiaryUseCase.kt`で単日取得→プレビュー用Markdown返却
  - `app/src/main/kotlin/domain/usecase/SaveDiaryUseCase.kt`で保存（最新SHA取得→PUT、409時にエラー種別化）
  - `app/src/main/kotlin/domain/usecase/ValidateTokenUseCase.kt`で設定保存時の接続テスト
  - 主要ユースケースごとに`app/src/test/kotlin/domain/usecase/*Test.kt`で成功/失敗/未設定ケースを検証

## プレゼンテーション層
- [x] ルーティング・状態管理の骨組み
  - `app/src/main/kotlin/ui/navigation/NavRoute.kt`で画面列挙（Calendar/Preview/Edit/Settings）
  - `app/src/main/kotlin/ui/AppViewModel.kt`でグローバル状態（選択日付、設定有無、ロード中、エラー）とナビゲーション制御
  - `app/src/main/kotlin/ui/AppScreen.kt`でScaffold（共通ヘッダー: 今日/SYNC/設定ボタン、ローディングインジケータ、トースト的エラー表示）

- [x] 設定ページ（PATフロー反映）
  - `app/src/main/kotlin/ui/settings/SettingsScreen.kt`でトークン入力、`GET /user`接続テスト、スコープ説明、リポ`org/repo`入力
  - `app/src/main/kotlin/ui/settings/SettingsViewModel.kt`で保存処理と検証、401/403時の再設定要求
  - `app/src/test/kotlin/ui/settings/SettingsViewModelTest.kt`で入力検証・保存成功/失敗のテスト

- [x] カレンダーページ
  - `app/src/main/kotlin/ui/calendar/CalendarScreen.kt`で日曜始まりカレンダー、月切替、未執筆/今日未投稿の表示、鉛筆アイコン
  - `app/src/main/kotlin/ui/calendar/CalendarViewModel.kt`で月次データ取得と今日判定、ロード/エラー管理
  - `app/src/test/kotlin/ui/calendar/CalendarViewModelTest.kt`で月切替・未設定・エラー時挙動テスト

- [x] 日記プレビューページ
  - `app/src/main/kotlin/ui/preview/PreviewScreen.kt`でMarkdown表示（GFM対応ライブラリ）、左矢印/鉛筆ボタン
  - `app/src/main/kotlin/ui/preview/PreviewViewModel.kt`で取得・ロード中表示・404時編集遷移トリガ
  - `app/src/test/kotlin/ui/preview/PreviewViewModelTest.kt`で既存/未存在/通信エラーのテスト

- [ ] 日記編集ページ
  - `app/src/main/kotlin/ui/edit/EditScreen.kt`でテキストエディタ（簡易マークダウン補助: 行頭`- `継続）、Saveボタン
  - `app/src/main/kotlin/ui/edit/EditViewModel.kt`で取得→初期文言`# yyyy/MM/dd (ddd)`、保存→プレビュー遷移、409時エラー提示
  - `app/src/test/kotlin/ui/edit/EditViewModelTest.kt`で新規/既存/409/未設定ケースのテスト

## その他・品質
- [ ] エラーメッセージとローディング共通化
  - `app/src/main/kotlin/ui/components/ErrorBanner.kt`、`LoadingOverlay.kt`を用意し各画面から共通利用
  - 軽微な国際化は不要（日本語固定）だが文言を`strings.kt`で一元管理
  - 簡易スナックバー/ダイアログは使わず画面内で表示（モーダル不要）

- [ ] シンク/再取得導線の実装
  - 共通ヘッダーSYNCボタンで現在画面の再取得をトリガーするハンドラをAppViewModelに実装
  - カレンダー表示月、プレビュー対象日、編集画面の再読込をケース分け

- [ ] ビルド/テストパイプライン整備
  - `app/src/test`配下のユニットテストをJUnit 6で実行するGradle設定（JUnit Platform 6.0.0）
  - `build.gradle.kts`にCI向けの`check`タスク設定、`ktlint`等の静的解析は任意でコメント記載
  - ローカル実行手順を`README.md`に追記（起動方法、GitHubトークン設定方法）
