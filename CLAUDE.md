# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

Run from the project root on Windows (use `./gradlew` on Mac/Linux):

```powershell
# Build
.\gradlew.bat assembleDebug
.\gradlew.bat bundleRelease

# Unit tests (JVM — includes WorkingDaysTest)
.\gradlew.bat :app:testDebugUnitTest

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
| `Constants.kt` | Processing-time ranges (working days) for each visa type. **Edit here when official times change.** |
| `Embassy.kt` | `Embassy` and `VacOption` data classes + all 7 embassy instances + `ALL_EMBASSIES` list. **Add/edit embassies and VAC offices here.** |
| `Holidays.kt` | Per-country public holiday sets for 2026/2027. Irish holidays are a shared private base; each country's set is stacked on top. Has `HOLIDAYS_LAST_UPDATED` constant. Floating holidays (Eid, Holi, Diwali, Chinese New Year, etc.) are approximate — verify each year. |
| `WorkingDays.kt` | `LocalDate.addWorkingDays(n, holidays)` and `workingDaysBetween(from, to, holidays)` — skip weekends + the provided holiday set. Default parameter = `ALL_HOLIDAYS` (India set) for backward compat with tests. |
| `ui/VisaTrackerScreen.kt` | Main screen: embassy dropdown, VAC radio cards, date input, visa-type selector, decision-window card, status-update buttons, "No decision yet" bottom sheet. |
| `ui/VisaGrantedScreen.kt` | Green celebration screen with Canvas confetti animation. |
| `ui/VisaRefusedScreen.kt` | Appeal countdown + `openCustomTab()` helper used across the app. |

### Supported embassies

| Embassy | Countries served | VAC offices |
|---|---|---|
| 🇮🇳 India (New Delhi) | India | Delhi (1 day), Other cities (2 days) |
| 🇷🇺 Russia/CIS (Moscow) | Russia, Kazakhstan, Uzbekistan, Kyrgyzstan, Tajikistan, Turkmenistan | Moscow (1 day), Almaty (2 days), Other CIS (3 days) |
| 🇬🇧 UK (London) | United Kingdom | London (1 day), Other UK (2 days) |
| 🇨🇳 China (Beijing) | China | Beijing (1 day), Shanghai (2 days), Other cities (3 days) |
| 🇹🇷 Turkey (Ankara) | Turkey | Ankara (1 day), Istanbul (2 days) |
| 🇦🇪 UAE (Abu Dhabi) | UAE and Gulf | Abu Dhabi (1 day), Dubai (2 days) |
| 🇵🇰 Pakistan (Islamabad) | Pakistan | Islamabad (1 day), Karachi (2 days), Lahore (2 days) |

Transit days are working days from VAC submission to the Irish Embassy receiving the file.

### Decision-window calculation
```
embassyReceiveDate  = submissionDate.addWorkingDays(vac.transitDays, embassy.holidays)
earliestDecision    = embassyReceiveDate.addWorkingDays(visaType.minDays, embassy.holidays)
latestDecision      = embassyReceiveDate.addWorkingDays(visaType.maxDays, embassy.holidays)
```
Day 1 of transit = the lodgment day itself (VAC submission date).
Holidays used = Irish public holidays + host-country public holidays (defined per embassy in `Holidays.kt`).

### Adding a new embassy
1. Add the country's holiday set to `Holidays.kt` (stack on top of `IRISH` private val).
2. Add an `Embassy(...)` instance to `Embassy.kt` with its `VacOption` list and decisions URL.
3. Append the instance to `ALL_EMBASSIES`.
4. Update `HOLIDAYS_LAST_UPDATED` in `Holidays.kt`.

### Theme
`ui/theme/IrishVisaExpectedDateTheme` supports Material You dynamic color on Android 12+ (API 31+), falling back to static colours. Dark mode follows system setting.

### Dependencies
- `java.time` (JSR-310) is available on API 24+ via `coreLibraryDesugaring` (enabled in `app/build.gradle.kts`).
- Chrome Custom Tabs: `androidx.browser:browser` — `openCustomTab()` is defined in `VisaRefusedScreen.kt`.
- All library versions live in `gradle/libs.versions.toml`.

**Key versions:** Kotlin 2.2.10 · AGP 9.2.1 · Compose BOM 2026.02.01 · minSdk 24 · compileSdk/targetSdk 36
