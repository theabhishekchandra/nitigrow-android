# NitiGrow Android

Native Android client for **NitiGrow** — the WhatsApp marketing & support
platform for Indian SMBs. A Websbaba Technologies product. Gives owners and
agents the inbox, notifications, campaigns, contacts, leads, analytics,
payments, and core dashboard on the go.

- **API:** `https://api.nitigrow.in` (see [`backend/`](../backend/))
- **Package:** `com.ardym.nitigrow`

## Tech stack

| | |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| DI | Hilt (Dagger) |
| Networking | Retrofit + OkHttp + Gson |
| Local DB | Room (with Paging 3) |
| Preferences | DataStore |
| Async | Kotlin Coroutines + Flow |
| Navigation | Navigation Compose |
| Images | Coil |
| Push | Firebase Cloud Messaging (FCM) |
| Crash reporting | Firebase Crashlytics |
| Performance | Firebase Performance |
| Analytics | Firebase Analytics |
| Payments | Razorpay Checkout |
| Widgets | Glance (home-screen widgets) |
| Background | WorkManager + Hilt integration |
| Logging | Timber |
| Min SDK | 26 (Android 8.0 — covers 95%+ of Indian devices) |
| Target SDK | 36 |
| JDK | 17 |

## Architecture

Clean Architecture with MVVM:

```
com.ardym.nitigrow/
├── core/            constants, extensions, utilities
├── data/            repositories, data sources, DTOs, Room DAOs
├── di/              Hilt modules
├── domain/          use cases, domain models
├── presentation/    feature screens (Compose + ViewModels)
├── ui/              theme, design system, shared composables
├── widget/          Glance home-screen widgets
├── work/            WorkManager workers
├── MainActivity.kt
└── NitiGrowApplication.kt
```

## Build & run

Requires **Android Studio** (latest) and **JDK 17**.

```bash
./gradlew assembleDebug      # build a debug APK
./gradlew installDebug       # build + install on a connected device/emulator
./gradlew test               # unit tests
./gradlew assembleStagingRelease  # staging build (minified, staging API)
./gradlew assembleRelease    # production build (requires signing config)
```

Or open the `android/` folder in **Android Studio** and Run ▶.

## Build variants

| Variant | API host | Minified | Signing |
|---------|----------|----------|---------|
| `debug` | `api.nitigrow.in` | No | debug keystore |
| `staging` | `staging.api.nitigrow.in` | Yes | debug keystore |
| `release` | `api.nitigrow.in` | Yes | release keystore (env/gradle props) |

Release signing requires `NITIGROW_STORE_FILE`, `NITIGROW_STORE_PASSWORD`,
`NITIGROW_KEY_ALIAS`, `NITIGROW_KEY_PASSWORD` set as Gradle properties or
environment variables.

## Key files

```
app/                   application module (Compose UI, networking, FCM)
build.gradle.kts       root Gradle config
settings.gradle.kts    module setup
gradle/                wrapper + version catalog (libs.versions.toml)
```

> `local.properties` (SDK path) and signing keystores are machine-specific and
> must not be committed.

## Deployment

Released to the Google Play Store. No GitHub-Actions auto-deploy — builds are
signed and uploaded as part of a release. Version code is driven by
`ANDROID_VERSION_CODE` env var in CI (falls back to `1` for local builds).

## Related

[Backend (`backend/`)](../backend/) · [iOS (`ios/`)](../ios/) ·
[Dashboard (`app/`)](../app/) · [SDK (`sdk/`)](../sdk/)
