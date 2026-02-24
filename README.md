# Time Keeper

Track the age of the people who matter most — in real-time, down to the second.

Whether you're watching your children grow, honoring the time you have with aging parents, or counting down to a milestone event, Time Keeper gives you a live, always-updating view of time's passage.

## Features

- **Real-time age tracking** — Live updates down to the second
- **Customizable display** — Mix and match years, months, days, hours, minutes, and seconds
- **Countdown mode** — Add future dates for automatic countdowns to events
- **Home screen widgets** — See live ages at a glance without opening the app
- **On-device AI messages** — Optional personalized messages generated entirely on-device using Qwen2 0.5B
- **Privacy first** — All data stored locally, no accounts, no analytics, no ads, no tracking

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose with Material Design 3
- **Database:** Room
- **Preferences:** DataStore
- **Background tasks:** WorkManager
- **AI:** Qwen2 0.5B GGUF (on-device inference)
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 35 (Android 15)

## Building

### Prerequisites

- Android Studio (latest stable)
- JDK 17

### Debug build

```bash
./gradlew assembleDebug
```

### Release build

```bash
./gradlew bundleRelease
```

Requires signing configuration. See [PLAY_STORE_SETUP.md](PLAY_STORE_SETUP.md) for details.

## CI/CD

The project uses GitHub Actions for automated builds and publishing:

- **Pull requests / dev pushes** — Lint, unit tests, debug APK build
- **Merge to main** — Build signed AAB and publish to Google Play internal testing track
- **Manual workflow dispatch** — Publish to any track (internal, alpha, beta, production)

## Project Structure

```
app/src/main/java/com/ageclock/app/
├── MainActivity.kt
├── ai/                    # On-device AI message generation
│   ├── MessageGenerator.kt
│   ├── ModelManager.kt
│   └── PromptBuilder.kt
├── data/
│   ├── local/             # Room database & DataStore
│   ├── model/             # Data classes (Person, CalculatedAge)
│   └── repository/        # Data access layer
└── ui/
    ├── home/              # Home screen & components
    ├── addperson/         # Add/edit person dialog
    └── ClockScreen.kt     # Main clock display
```

## Privacy

All data is stored locally on your device. The only network request is an optional one-time download of the AI model from Hugging Face. No data is ever sent to any server.

[Full Privacy Policy](docs/privacy-policy.html)

## License

All rights reserved.
