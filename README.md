# NitiGrow Android

Native Android client for **NitiGrow** — the WhatsApp marketing & support
platform for Indian SMBs. A Websbaba Technologies product. Gives owners and
agents the inbox, notifications, and core dashboard on the go.

- **API:** `https://api.nitigrow.in` (see `nitigrow-backend`)

## Tech stack

Kotlin · Jetpack Compose · Gradle (Kotlin DSL) · FCM push notifications.

## Build & run

Requires Android Studio (latest) and JDK 17.

```bash
# from the project root
./gradlew assembleDebug      # build a debug APK
./gradlew installDebug       # build + install on a connected device/emulator
./gradlew test               # unit tests
```

Or open the folder in **Android Studio** and Run ▶.

```
app/                  application module (Compose UI, networking, FCM)
build.gradle.kts      root Gradle config
settings.gradle.kts   module setup
gradle/               wrapper + version catalog
```

> `local.properties` (SDK path) and signing keystores are machine-specific and
> must not be committed.

## Deployment

Released to the Google Play Store. No GitHub-Actions auto-deploy — builds are
signed and uploaded as part of a release.

## Related repos

`nitigrow-backend` (API) · `nitigrow-ios` (iOS client) ·
`nitigrow-app` (web dashboard) · `nitigrow-sdk`
