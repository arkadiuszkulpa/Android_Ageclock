# Claude Android Development Notes

This file contains learnings and reminders for Claude when working on Android projects.
**Last Updated:** January 2026

---

## Version Research - ALWAYS CHECK FIRST

Before starting any Android project, **always use web search** to find the latest stable versions of:

1. **Android Gradle Plugin (AGP)** - Check [AGP Release Notes](https://developer.android.com/build/releases/gradle-plugin)
2. **Kotlin** - Check [Kotlin Releases](https://kotlinlang.org/docs/releases.html)
3. **Compose BOM** - Check [Compose BOM Mapping](https://developer.android.com/develop/ui/compose/bom/bom-mapping)
4. **Gradle** - Must be compatible with AGP version

**Why?** Claude's training data may have outdated library versions. Using old versions causes build failures.

---

## Current Recommended Versions (January 2026)

| Component | Version | Compatibility Notes |
|-----------|---------|---------------------|
| AGP | 8.7.0 | Stable, works with most Android Studio versions |
| AGP | 9.0.0 | Requires Android Studio Otter 3 Feature Drop (2025.2.3+) |
| Kotlin | 2.0.21 | Use with AGP 8.x |
| Compose BOM | 2025.01.00 or 2026.01.00 | Manages all Compose library versions |
| Gradle | 8.9 | Compatible with AGP 8.7 |
| Gradle | 9.1 | For AGP 9.0 |

---

## Key Learnings

### 1. Use Version Catalogs (libs.versions.toml)
Modern Android projects use Gradle version catalogs for dependency management:
- Location: `gradle/libs.versions.toml`
- Reference in build.gradle.kts: `libs.plugins.android.application`, `libs.androidx.core.ktx`

### 2. AGP 9.0 Built-in Kotlin
AGP 9.0+ has **built-in Kotlin support** - no need for `org.jetbrains.kotlin.android` plugin.
However, for broader compatibility, use AGP 8.x with explicit Kotlin plugin.

### 3. Compose Compiler Plugin
Kotlin 2.0+ requires the separate Compose compiler plugin:
```kotlin
alias(libs.plugins.kotlin.compose)
```

### 4. SDK Versions
- **minSdk 26** (Android 8.0) - Good balance of modern APIs and device coverage
- **targetSdk 35** (Android 15) - Required for Google Play as of 2025/2026
- **compileSdk 35** - Latest stable

### 5. Jetpack Compose Best Practices
- Use `LaunchedEffect` for side effects (like timers)
- Use `remember` for state that survives recomposition
- Use `mutableStateOf` or `mutableLongStateOf` for observable state
- Material3 is the current design system

### 6. Project Structure
```
project/
├── app/
│   ├── src/main/
│   │   ├── java/com/package/app/
│   │   │   ├── MainActivity.kt
│   │   │   └── ui/
│   │   │       ├── theme/
│   │   │       └── screens/
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
│   ├── wrapper/gradle-wrapper.properties
│   └── libs.versions.toml
├── build.gradle.kts (root)
├── settings.gradle.kts
└── gradle.properties
```

---

## Common Pitfalls

1. **Outdated versions** - Always web search for latest
2. **Missing Compose compiler plugin** - Required with Kotlin 2.0+
3. **Wrong Gradle version** - Must match AGP compatibility
4. **Missing `enableEdgeToEdge()`** - Call in Activity for modern edge-to-edge UI
5. **Forgetting platform BOM** - Use `platform(libs.androidx.compose.bom)` for Compose

---

## Useful Commands

```bash
# Check Gradle version
./gradlew --version

# Build project
./gradlew build

# Clean build
./gradlew clean build

# Run debug build
./gradlew assembleDebug

# List dependencies
./gradlew app:dependencies
```

---

## Resources

- [Android Developers](https://developer.android.com/)
- [Jetpack Compose Docs](https://developer.android.com/develop/ui/compose)
- [Compose BOM](https://developer.android.com/develop/ui/compose/bom)
- [Material3 for Compose](https://developer.android.com/develop/ui/compose/designsystems/material3)
- [Kotlin Docs](https://kotlinlang.org/docs/home.html)
