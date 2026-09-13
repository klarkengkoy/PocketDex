# PocketDex

## Project Overview
PocketDex is a retro-styled Pokémon encyclopedia built to explore the capabilities of **Shared Element Transitions** and **High-Performance Rendering** in Jetpack Compose. Inspired by the classic GameBoy aesthetic, it blends nostalgic visual fidelity with a modern, reactive technical engine. Powered by the [PokéAPI](https://pokeapi.co/), the project focuses on data accuracy, "Pixel Perfect" rendering, and fluid UI motion.

## Core Features
- **Retro Aesthetic:** Immersive checkered backgrounds powered by hardware-accelerated shaders and edge-to-edge rendering.
- **Shared Element Transitions:** Seamless "flight" animations for Pokémon sprites between screens using the Compose Shared Transition API.
- **Interactive Discovery:** Features a custom **Morphing Search Bar** and **Type Filter Bottom Sheet** for fluid, app-like interactions.
- **Adaptive Layouts:** Fully optimized for system insets and edge-to-edge rendering with a custom unified navigation system.
- **Evolution Hub:** ID-sorted carousel supporting linear and branching families with a "Stable Container" strategy to prevent UI blinking.
- **Offline-First Data:** Full Room persistence with a **WorkManager-powered Sync Engine** that reliably backfills data in the background.
- **Modern DI Architecture:** Clean dependency management using **Hilt** for scalability and testability.
- **Lifecycle Optimized:** Resource-efficient state management using `collectAsStateWithLifecycle`.

## Technical Stack
- **Kotlin 2.4.20** & **KSP 2.3.10**
- **Jetpack Compose (Material 3)**
- **Jetpack Navigation 3** (Hilt-integrated routing)
- **Hilt DI** (Dagger-based)
- **WorkManager** (Coroutine-based sync)
- **Room Database 2.8.4**
- **Retrofit & Kotlinx Serialization**
- **Testing:** Turbine, Mockito-Kotlin, Compose UI Testing