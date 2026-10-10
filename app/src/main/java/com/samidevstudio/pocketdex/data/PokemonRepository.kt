package com.samidevstudio.pocketdex.data

import com.samidevstudio.pocketdex.data.database.EvolutionChainEntity
import com.samidevstudio.pocketdex.data.database.PokemonDao
import com.samidevstudio.pocketdex.data.database.toDetailModel
import com.samidevstudio.pocketdex.data.database.toEntity
import com.samidevstudio.pocketdex.data.database.toUiModel
import com.samidevstudio.pocketdex.ui.pokemon.EvolutionNode
import com.samidevstudio.pocketdex.ui.pokemon.PokemonDetailModel
import com.samidevstudio.pocketdex.ui.pokemon.PokemonUiModel
import com.samidevstudio.pocketdex.ui.pokemon.StatInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

interface PokemonRepository {
    fun getPokemonListFlow(): Flow<List<PokemonUiModel>>
    suspend fun fetchPokemonList(offset: Int, limit: Int = 60)
    
    fun getPokemonDetailFlow(id: String): Flow<PokemonDetailModel?>
    fun getEvolutionChainFlow(chainId: String): Flow<List<EvolutionNode>>
    
    suspend fun syncPokemonDetail(id: String)
    suspend fun syncEvolutionChain(chainId: String)
    suspend fun backfillMissingTypes()
}

class DefaultPokemonRepository @Inject constructor(
    private val apiService: PokeApiService,
    private val pokemonDao: PokemonDao
) : PokemonRepository {

    companion object {
        internal fun extractChainIdFromUrl(url: String?): String {
            if (url.isNullOrBlank()) return ""
            return url.trimEnd('/').split('/').lastOrNull { it.isNotBlank() } ?: ""
        }
    }

    private var isBackfilling = false
    private val inFlightDetailSyncs = mutableSetOf<String>()
    private val inFlightListFetches = mutableSetOf<Pair<Int, Int>>()

    override fun getPokemonListFlow(): Flow<List<PokemonUiModel>> {
        return pokemonDao.getPokemonList(1000, 0).map { list -> 
            list.map { it.toUiModel() } 
        }
    }
    
    override suspend fun fetchPokemonList(offset: Int, limit: Int) {
        val fetchKey = offset to limit
        if (inFlightListFetches.contains(fetchKey)) return
        inFlightListFetches.add(fetchKey)

        try {
            val count = pokemonDao.getPokemonCountInRange(offset, limit)
            if (count >= limit) return

            val response = apiService.getPokemonList(offset = offset, limit = limit)
            if (response.results.isEmpty()) return

            val newItems = response.results.map { item ->
                PokemonUiModel(
                    id = item.id,
                    name = item.name,
                    imageUrl = item.imageUrl,
                    types = emptyList()
                )
            }
            pokemonDao.insertPokemonList(newItems.map { it.toEntity() })
        } finally {
            inFlightListFetches.remove(fetchKey)
        }
    }

    override fun getPokemonDetailFlow(id: String): Flow<PokemonDetailModel?> {
        return pokemonDao.getPokemonDetailFlow(id).map { entity ->
            entity?.toDetailModel(evolutions = emptyList())
        }
    }

    override fun getEvolutionChainFlow(chainId: String): Flow<List<EvolutionNode>> {
        return pokemonDao.getEvolutionChainFlow(chainId).map { entity ->
            entity?.evolutions?.takeIf { it.size == 1 } ?: emptyList()
        }
    }

    override suspend fun syncPokemonDetail(id: String) {
        if (inFlightDetailSyncs.contains(id)) return
        inFlightDetailSyncs.add(id)

        try {
            val existing = pokemonDao.getPokemonDetail(id)
            if (existing != null && existing.chainId.isNotEmpty()) {
                val storedTypes = pokemonDao.getPokemonTypes(id)
                val hasTypes = !storedTypes.isNullOrEmpty() || existing.types.isNotEmpty()
                if (storedTypes.isNullOrEmpty() && existing.types.isNotEmpty()) {
                    pokemonDao.updatePokemonTypes(id, existing.types)
                }
                val cachedChain = pokemonDao.getEvolutionChain(existing.chainId)
                if (cachedChain == null || cachedChain.evolutions.size != 1) {
                    syncEvolutionChain(existing.chainId)
                }
                if (hasTypes) return
            }

            val networkDetail = apiService.getPokemonDetail(id)
            val speciesResponse = apiService.getPokemonSpecies(id)
            val flavorText = speciesResponse.flavorTextEntries
                .firstOrNull { it.language.name == "en" }
                ?.flavorText?.replace("\n", " ")?.replace("\u000c", " ") ?: ""

            val chainId = extractChainIdFromUrl(speciesResponse.evolutionChain.url)
            if (chainId.isBlank()) {
                pokemonDao.insertPokemonDetail(networkDetail.toPokemonDetailModel(flavorText, emptyList(), "").toEntity(id, ""))
                return
            }

            val detailModel = networkDetail.toPokemonDetailModel(flavorText, emptyList(), chainId)
            pokemonDao.insertPokemonDetail(detailModel.toEntity(id, chainId))
            pokemonDao.updatePokemonTypes(id, detailModel.types)

            val localChain = pokemonDao.getEvolutionChain(chainId)
            if (localChain == null || localChain.evolutions.size != 1) {
                syncEvolutionChain(chainId)
            }
        } finally {
            inFlightDetailSyncs.remove(id)
        }
    }

    override suspend fun syncEvolutionChain(chainId: String) {
        val url = "https://pokeapi.co/api/v2/evolution-chain/$chainId/"
        val evolutionChainResponse = apiService.getEvolutionChain(url)
        val evolutionRoot = mapEvolutionChain(evolutionChainResponse.chain)

        pokemonDao.insertEvolutionChain(
            EvolutionChainEntity(id = chainId, evolutions = listOf(evolutionRoot))
        )
    }

    override suspend fun backfillMissingTypes() {
        if (isBackfilling) return
        isBackfilling = true

        try {
            for (id in pokemonDao.getPokemonIdsMissingTypes()) {
                syncPokemonDetail(id)
                delay(100.milliseconds)
            }
        } finally {
            isBackfilling = false
        }
    }

    private fun mapEvolutionChain(chain: ChainLink): EvolutionNode =
        EvolutionNode(
            id = chain.species.id,
            name = chain.species.name,
            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${chain.species.id}.png",
            evolvesTo = chain.evolvesTo.map(::mapEvolutionChain)
        )

    private fun PokemonDetail.toPokemonDetailModel(
        flavorText: String,
        evolutions: List<EvolutionNode>,
        chainId: String
    ): PokemonDetailModel {
        return PokemonDetailModel(
            id = this.id.toString(),
            name = this.name,
            imageUrl = this.sprites.other.officialArtwork.frontDefault,
            types = this.types.map { it.type.name },
            height = this.height,
            weight = this.weight,
            stats = this.stats.map { 
                val displayName = when (it.stat.name.lowercase()) {
                    "hp" -> "HP"
                    "attack" -> "Attack"
                    "defense" -> "Defense"
                    "special-attack" -> "Sp. Atk"
                    "special-defense" -> "Sp. Def"
                    "speed" -> "Speed"
                    else -> it.stat.name.split("-").joinToString(" ") { word -> 
                        word.replaceFirstChar { char -> char.uppercase() } 
                    }
                }
                StatInfo(
                    name = displayName,
                    value = it.baseStat
                )
            },
            flavorText = flavorText,
            evolutions = evolutions,
            chainId = chainId
        )
    }
}
