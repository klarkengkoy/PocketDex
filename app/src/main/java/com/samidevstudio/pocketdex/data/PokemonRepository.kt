package com.samidevstudio.pocketdex.data

import com.samidevstudio.pocketdex.ui.PokemonDetailModel
import com.samidevstudio.pocketdex.ui.PokemonUiModel
import com.samidevstudio.pocketdex.ui.StatInfo

/**
 * Interface defining the operations for fetching Pokémon data.
 * Adhering to the Repository pattern for clean architecture.
 */
interface PokemonRepository {
    suspend fun getPokemonList(offset: Int, limit: Int = 20): List<PokemonUiModel>
    suspend fun getPokemonDetail(id: String): PokemonDetailModel
}

/**
 * Default implementation of PokemonRepository that fetches data from the PokéAPI.
 * Now includes an in-memory cache to avoid redundant GET calls.
 */
class DefaultPokemonRepository(private val apiService: PokeApiService = RetrofitClient.pokeApiService) : PokemonRepository {

    // Simple in-memory cache to store already-fetched details
    private val detailCache = mutableMapOf<String, PokemonDetailModel>()

    override suspend fun getPokemonList(offset: Int, limit: Int): List<PokemonUiModel> {
        val response = apiService.getPokemonList(offset = offset, limit = limit)
        return response.results.map { item ->
            PokemonUiModel(
                id = item.id,
                name = item.name,
                imageUrl = item.imageUrl,
                types = emptyList() // Types are backfilled later by the ViewModel
            )
        }
    }

    /**
     * Fetches details, checking the cache first to prevent redundant network traffic.
     */
    override suspend fun getPokemonDetail(id: String): PokemonDetailModel {
        // Step 1: Check the cache
        detailCache[id]?.let {
            return it
        }

        // Step 2: If not in cache, fetch from network
        val networkDetail = apiService.getPokemonDetail(id)
        val detailModel = networkDetail.toPokemonDetailModel()

        // Step 3: Save to cache and return
        detailCache[id] = detailModel
        return detailModel
    }

    /**
     * Extension function to convert network model to UI model.
     * Centralizing mapping logic in the repository layer.
     */
    private fun PokemonDetail.toPokemonDetailModel(): PokemonDetailModel {
        return PokemonDetailModel(
            id = this.id.toString(),
            name = this.name,
            imageUrl = this.sprites.other.officialArtwork.frontDefault,
            types = this.types.map { it.type.name },
            height = this.height,
            weight = this.weight,
            stats = this.stats.map { 
                StatInfo(
                    name = it.stat.name.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase() else char.toString() },
                    value = it.baseStat
                )
            }
        )
    }
}
