package com.samidevstudio.pocketdex.data

import com.samidevstudio.pocketdex.ui.pokemon.EvolutionNode
import com.samidevstudio.pocketdex.ui.pokemon.PokemonDetailModel
import com.samidevstudio.pocketdex.ui.pokemon.PokemonUiModel
import com.samidevstudio.pocketdex.ui.pokemon.StatInfo

interface PokemonRepository {
    suspend fun getPokemonList(offset: Int, limit: Int = 20): List<PokemonUiModel>
    suspend fun getPokemonDetail(id: String): PokemonDetailModel
    fun getCachedPokemonDetail(id: String): PokemonDetailModel?
}

class DefaultPokemonRepository(private val apiService: PokeApiService = RetrofitClient.pokeApiService) : PokemonRepository {

    private val detailCache = mutableMapOf<String, PokemonDetailModel>()

    override suspend fun getPokemonList(offset: Int, limit: Int): List<PokemonUiModel> {
        val response = apiService.getPokemonList(offset = offset, limit = limit)
        return response.results.map { item ->
            PokemonUiModel(
                id = item.id,
                name = item.name,
                imageUrl = item.imageUrl,
                types = emptyList() // Backfilled later by ViewModel
            )
        }
    }

    override suspend fun getPokemonDetail(id: String): PokemonDetailModel {
        detailCache[id]?.let {
            if (it.flavorText.isNotEmpty()) return it
        }

        val networkDetail = apiService.getPokemonDetail(id)
        val speciesResponse = apiService.getPokemonSpecies(id)
        val flavorText = speciesResponse.flavorTextEntries
            .firstOrNull { it.language.name == "en" }
            ?.flavorText?.replace("\n", " ")?.replace("\u000c", " ") ?: ""
        
        val evolutionChainResponse = apiService.getEvolutionChain(speciesResponse.evolutionChain.url)
        val evolutions = flattenEvolutionChain(evolutionChainResponse.chain)

        val detailModel = networkDetail.toPokemonDetailModel(flavorText, evolutions)

        detailCache[id] = detailModel
        return detailModel
    }

    override fun getCachedPokemonDetail(id: String): PokemonDetailModel? {
        return detailCache[id]
    }

    /**
     * Digs through the recursive evolution chain to create a flat list of nodes.
     */
    private fun flattenEvolutionChain(chain: ChainLink): List<EvolutionNode> {
        val nodes = mutableListOf<EvolutionNode>()
        nodes.add(
            EvolutionNode(
                id = chain.species.id,
                name = chain.species.name,
                imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${chain.species.id}.png"
            )
        )
        chain.evolvesTo.forEach {
            nodes.addAll(flattenEvolutionChain(it))
        }
        return nodes
    }

    private fun PokemonDetail.toPokemonDetailModel(
        flavorText: String,
        evolutions: List<EvolutionNode>
    ): PokemonDetailModel {
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
            },
            flavorText = flavorText,
            evolutions = evolutions
        )
    }
}
