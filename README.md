# PocketDex

## Project Overview
PocketDex is a retro-styled Pokémon encyclopedia built to explore the capabilities of **Shared Element Transitions** and **High-Performance Rendering** in Jetpack Compose. Inspired by the classic GameBoy aesthetic, it blends nostalgic visual fidelity with a modern, reactive technical engine. Powered by the [PokéAPI](https://pokeapi.co/), the project focuses on data accuracy, "Pixel Perfect" rendering, and fluid UI motion.

## Core Features
- **Retro Aesthetic:** Immersive checkered backgrounds powered by hardware-accelerated shaders and edge-to-edge rendering.
- **Shared Element Transitions:** Seamless "flight" animations for Pokémon sprites between screens using the Compose Shared Transition API.
- **Evolution Hub:** ID-sorted carousel supporting linear and branching families with a "Stable Container" strategy to prevent UI blinking.
- **Symmetric Grid:** Perfectly balanced card layout with "Heroic" sprite presence and smart text wrapping for long names.
- **High-Performance Architecture:** 120FPS fluid scrolling achieved through model stability (@Immutable), draw caching, and lazy shared elements.
- **Offline-First Data:** Full Room persistence with a "Polite Sync" engine that backfills Pokémon types and data in the background.

## Technical Stack
- **Kotlin 2.4.10** & **KSP 2.3.10**
- **Jetpack Compose (Material 3)**
- **Jetpack Navigation 3** (Parcelize-based routing)
- **Room Database 2.8.4** (Reactive Flow-based schema)
- **Retrofit & Kotlinx Serialization**
- **Coroutines & StateFlow** (Reactive pipeline management)
- **Coil** (Pixel-accurate image transformations)

---
*Modern Engineering. Retro Soul.*
