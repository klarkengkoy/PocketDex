# PocketDex

## Project Overview
PocketDex is a retro-styled Pokémon encyclopedia built to explore the capabilities of **Shared Element Transitions** in Jetpack Compose. Inspired by the classic GameBoy aesthetic, it blends nostalgic visual fidelity with a modern technical engine. Powered by the [PokéAPI](https://pokeapi.co/), the project focuses on data accuracy and "Pixel Perfect" rendering, featuring crisp sprites and fluid UI motion.

## Core Features
- **Retro Aesthetic:** Checkered backgrounds and edge-to-edge immersive UI with nearest-neighbor sprite scaling.
- **Shared Element Transitions:** Seamless "flight" animations for Pokémon sprites between screens using the Compose Shared Transition API.
- **Evolution Hub:** ID-sorted carousel supporting linear and branching families (e.g., Eevee) with reactive "pop-in" sibling updates.
- **Offline-First Architecture:** Full Room persistence ensuring instant data access and full functionality without a network connection.
- **Smart Sync:** Background evolution crawler with exponential backoff and reactive pre-fetching for the main list.

## Technical Stack
- **Kotlin 2.4.10** & **KSP 2.3.10**
- **Jetpack Compose (Material 3)**
- **Jetpack Navigation 3** (Parcelize-based routing)
- **Room Database 2.8.4** (Reactive Flow-based schema)
- **Retrofit & Kotlinx Serialization**
- **Coroutines & StateFlow** (Reactive pipeline management)
- **Coil** (Pixel-accurate image transformations)

---

