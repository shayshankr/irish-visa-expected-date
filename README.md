# Irish Visa Expected Date

An Android app that estimates when you can expect a decision on your Irish visa application based on your VFS Global submission date.

> **DISCLAIMER:** This is an unofficial app. All dates are estimates only and may not reflect actual processing times. Always verify with the [Embassy of Ireland](https://www.ireland.ie/en/india/newdelhi/) directly. This app is not affiliated with, endorsed by, or connected to the Embassy of Ireland or VFS Global in any way.

---

## Features

- Enter your VFS Global (VAC) submission date
- Select VAC location (New Delhi, Mumbai, etc.)
- Select visa type (Tourist, Study, Work, etc.)
- Calculates the embassy receive date accounting for VAC transit days
- Shows the earliest and latest expected decision dates
- Accounts for Irish and Indian public holidays (weekends excluded)
- Track your application status: waiting, approved, or refused
- Appeal deadline countdown on refusal
- Celebration screen on approval with confetti animation
- Material You dynamic colour (Android 12+), dark mode support

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 36
- **Architecture:** Single-activity, no Navigation component — screen state via `enum class AppScreen`
- **Date handling:** `java.time` (JSR-310) via core library desugaring

## Building

Requires Android Studio (Ladybug or newer) or the Android command-line tools.

```powershell
# Windows
.\gradlew.bat assembleDebug

# macOS / Linux
./gradlew assembleDebug
```

The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

### Run unit tests

```powershell
.\gradlew.bat :app:test
```

## Updating processing times

All processing-time ranges and VAC transit-day constants live in `app/src/main/java/com/example/irishvisaexpecteddate/Constants.kt`. Edit that file when official times change.

Public holidays for 2026/2027 are in `Holidays.kt`. Floating holidays (Eid, Holi, Diwali, etc.) are approximate — verify each year.

## License

This project is licensed under the [MIT License](LICENSE).
