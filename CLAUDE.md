# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

Run from the project root on Windows (use `./gradlew` on Mac/Linux):

```powershell
# Build
.\gradlew.bat assembleDebug
.\gradlew.bat assembleRelease

# Unit tests (JVM — includes WorkingDaysTest)
.\gradlew.bat :app:test
.\gradlew.bat :app:test --tests "com.example.irishvisaexpecteddate.WorkingDaysTest"

# Instrumented tests (requires connected device/emulator)
.\gradlew.bat connectedAndroidTest

# Lint
.\gradlew.bat :app:lint
```

## Architecture

Single-module Android app (`app`). No fragments — all UI is Jetpack Compose. Screen navigation is a plain `enum class AppScreen` state variable in `MainActivity`; no Navigation component.

### Key files

| File | Purpose |
|---|---|
| `Constants.kt` | All editable processing-time ranges and VAC transit-day constants. **Edit here when official times change.** |
| `Holidays.kt` | Hardcoded 2026/2027 Irish + Indian public holiday sets. Has `HOLIDAYS_LAST_UPDATED` constant. Floating holidays (Eid, Holi, Diwali, etc.) are approximate — verify each year. |
| `WorkingDays.kt` | `LocalDate.addWorkingDays(n)` and `workingDaysBetween(from, to)` — skip weekends + `ALL_HOLIDAYS`. |
| `ui/VisaTrackerScreen.kt` | Main screen: date input, VAC/visa-type selectors, decision-window card, status-update buttons, and "No decision yet" bottom sheet. |
| `ui/VisaGrantedScreen.kt` | Green celebration screen with Canvas confetti animation. |
| `ui/VisaRefusedScreen.kt` | Appeal countdown + `openCustomTab()` helper used across the app. |

### Decision-window calculation
```
embassyReceiveDate  = submissionDate.addWorkingDays(vac.transitDays)
earliestDecision    = embassyReceiveDate.addWorkingDays(visaType.minDays)
latestDecision      = embassyReceiveDate.addWorkingDays(visaType.maxDays)
```
Day 1 of transit = the lodgment day itself (VAC submission date).

### Theme
`ui/theme/IrishVisaExpectedDateTheme` supports Material You dynamic color on Android 12+ (API 31+), falling back to static colours. Dark mode follows system setting.

### Dependencies
- `java.time` (JSR-310) is available on API 24+ via `coreLibraryDesugaring` (enabled in `app/build.gradle.kts`).
- Chrome Custom Tabs: `androidx.browser:browser` — `openCustomTab()` is defined in `VisaRefusedScreen.kt`.
- All library versions live in `gradle/libs.versions.toml`.

**Key versions:** Kotlin 2.2.10 · AGP 9.2.1 · Compose BOM 2026.02.01 · minSdk 24 · compileSdk/targetSdk 36
