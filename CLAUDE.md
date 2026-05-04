# NitiGrow Android App — Claude Instructions

## What Is This?
Native Android app for NitiGrow clients.
Play Store package: `com.nitigrow.app`
Minimum SDK: Android 8.0 (API 26) — covers 95%+ of Indian devices.

## Tech Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Architecture:** MVVM (Model → Repository → ViewModel → UI)
- **Networking:** Retrofit2 + OkHttp3
- **Serialization:** Gson
- **Image loading:** Coil
- **DI:** Hilt (dependency injection)
- **Local DB:** Room (offline support)
- **Preferences:** DataStore
- **Push:** Firebase Cloud Messaging (FCM)
- **Real-time:** OkHttp WebSocket → Socket.io
- **Async:** Coroutines + Flow
- **Navigation:** Navigation Compose
- **Background:** WorkManager

## Base URL
`https://api.nitigrow.in`

## Project Structure (Build This)
```
app/
├── data/
│   ├── api/          Retrofit API interfaces
│   ├── models/       Data classes
│   ├── repository/   Data access layer
│   └── local/        Room database
├── ui/
│   ├── screens/      Compose screens
│   ├── components/   Reusable UI components
│   └── theme/        Colors, typography
└── viewmodels/       MVVM ViewModels
```

## Key Screens (Priority Order)
1. **Login / OTP** — biometric support
2. **Inbox** — WhatsApp-style conversation list + chat window (most important)
3. **Broadcasts** — create + view campaigns
4. **Contacts** — list + add contact
5. **Dashboard** — stats summary
6. **Templates** — view + pick templates

## Critical Rules
- Inbox MUST feel like WhatsApp — same speed, same double-tick animations
- JWT token stored in DataStore (encrypted) — never SharedPreferences plain text
- All API calls go through OkHttp interceptor that adds `Authorization: Bearer <token>`
- Media size limits enforced BEFORE upload (Image: 5MB, Video: 16MB, Doc: 100MB)
- Offline support: Room DB caches conversations + contacts
- ProGuard rules must be included (clients minify their builds)

## Push Notifications
- FCM token registered with NitiGrow backend on login
- Notifications received even when app is closed
- Tap notification → opens specific conversation

## Key Phase Doc
- `../docs/phase-3-mobile.md` — complete Android + iOS build checklist
