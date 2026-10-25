# PocketDex

PocketDex is a retro-styled Pokémon encyclopedia for Android. It uses PokéAPI and a Game Boy-inspired interface to browse Pokémon, moves, items, and Explore resources, and to build and compare teams in Team Lab.

<p align="center">
  <a href="https://klarkengkoy.github.io/klarkengkoy/#pocketdex-title"><img src="assets/showcase-button.svg" alt="View PocketDex Showcase" width="300" height="56" /></a>
</p>

## Features

- Five unified bottom navigation destinations: Explore, Moves, central Poké Ball (Pokémon), Items, and Team Lab.
- Explore hub providing access to Types, Abilities, Berries, Regions, Locations, and an interactive Evolution family tree lookup.
- View Pokémon details, stats, types, and evolution families with shared-element transitions; swipe horizontally along an evolution path and vertically between branches, or use the branch buttons.
- Build a team of up to six Pokémon in Team Lab, inspect type-based team coverage and weaknesses, and compare any two Pokémon side-by-side without mutating your saved roster.
- Cache reference data locally with Room across database migrations and synchronize missing Pokémon type data in the background.
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
