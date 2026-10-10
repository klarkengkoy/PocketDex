# PocketDex

PocketDex is a retro-styled Pokémon encyclopedia for Android. It uses PokéAPI and a Game Boy-inspired interface to browse Pokémon, moves, types, and items, and to build a local team.

## Features

- Browse and search Pokémon, moves, and items.
- View Pokémon details, stats, types, and evolution families with shared-element transitions; swipe horizontally along an evolution path and vertically between branches, or use the branch buttons.
- Explore type effectiveness and item details.
- Build a team of up to six Pokémon and review its type coverage.
- Cache reference data locally with Room and synchronize missing Pokémon type data in the background.
- Use a custom retro interface with edge-to-edge layouts and animated navigation.

## Technology

- Kotlin and Jetpack Compose with Material 3
- Navigation 3 and Hilt
- Room, Retrofit, and Kotlin serialization
- WorkManager for periodic background sync
- JUnit, Turbine, Mockito-Kotlin, and Compose UI testing

## Build

Open the project in Android Studio with the required Android SDK installed, or use the Gradle wrapper:

```powershell
gradlew.bat :app:assembleDebug
gradlew.bat :app:testDebugUnitTest
```

On macOS or Linux, use `./gradlew` in place of `gradlew.bat`.
