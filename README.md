# 📟 PocketDex

**PocketDex** is a high-performance Pokémon encyclopedia inspired by the classic retro "GameBoy" aesthetic. Powered by the [PokéAPI](https://pokeapi.co/), it provides a deep-dive into the Pokémon universe with a focus on data accuracy and "Pixel Perfect" visual fidelity.

## 🎨 Design Vision: "Pixel Perfection"
*   **Retro Aesthetic:** Hand-crafted checkered backgrounds and transparent cards with 2dp black borders.
*   **Sharp Rendering:** 1:1 square proportions and "Nearest Neighbor" scaling for crisp, pixelated sprites.
*   **Edge-to-Edge:** A fully immersive experience where the interface bleeds to the screen boundaries for maximum focus on the artwork.

## 🛠️ The Core Engine
*   **Data Source:** Built using the extensive PokéAPI database.
*   **Clean Architecture:**
    *   **Repository Pattern:** A decoupled data layer for reliable information retrieval.
    *   **In-Memory Caching:** Smart temporary storage to eliminate redundant network traffic.
    *   **Parallel Pipeline:** Optimized data fetching that loads basic information instantly and enriches details in the background.

## 🗺️ Roadmap: The Pokeball Hub
The project is evolving toward a unique 5-tab hub system:
*   **The Bag:** A dedicated encyclopedia for items and berries.
*   **Move-Dex:** A searchable library of attacks and abilities.
*   **The Pokeball (Center):** A large, custom-drawn Hero button docked in a "cradle" cutout for the primary Dex navigation.
*   **Strategy:** A retro-styled type effectiveness chart for tactical reference.
*   **Options:** Trainer profile and local data management.

---
*Developed with a focus on performance, data integrity, and retro soul.*
