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
import retrofit2.HttpException
import kotlin.time.Duration.Companion.milliseconds

interface PokemonRepository {
    fun getPokemonListFlow(): Flow<List<PokemonUiModel>>
    suspend fun fetchPokemonList(offset: Int, limit: Int = 60)
    
    fun getPokemonDetailFlow(id: String): Flow<PokemonDetailModel?>
    fun getEvolutionChainFlow(chainId: String): Flow<List<EvolutionNode>>
    
    suspend fun syncPokemonDetail(id: String)
    suspend fun syncEvolutionChain(chainId: String)
    suspend fun startEvolutionChainCrawler()
}

class DefaultPokemonRepository(
    private val apiService: PokeApiService = RetrofitClient.pokeApiService,
    private val pokemonDao: PokemonDao
) : PokemonRepository {

    override fun getPokemonListFlow(): Flow<List<PokemonUiModel>> {
        return pokemonDao.getPokemonList(1000, 0).map { list -> 
            list.map { it.toUiModel() } 
        }
    }
    
    override suspend fun fetchPokemonList(offset: Int, limit: Int) {
        try {
            // Check if we already have these items in DB to avoid redundant network calls
            val count = pokemonDao.getPokemonCountInRange(offset, limit)
            if (count >= limit) return // Already have this batch

            val response = apiService.getPokemonList(offset = offset, limit = limit)
            val newItems = response.results.map { item ->
                PokemonUiModel(
                    id = item.id,
                    name = item.name,
                    imageUrl = item.imageUrl,
                    types = emptyList()
                )
            }
            pokemonDao.insertPokemonList(newItems.map { it.toEntity() })
        } catch (_: Exception) {
            // Handle error
        }
    }

    override fun getPokemonDetailFlow(id: String): Flow<PokemonDetailModel?> {
        return pokemonDao.getPokemonDetailFlow(id).map { entity ->
            entity?.toDetailModel(evolutions = emptyList())
        }
    }

    override fun getEvolutionChainFlow(chainId: String): Flow<List<EvolutionNode>> {
        return pokemonDao.getEvolutionChainFlow(chainId).map { entity ->
            entity?.evolutions ?: emptyList()
        }
    }

    override suspend fun syncPokemonDetail(id: String) {
        try {
            // Check if we already have the detail in DB
            val existing = pokemonDao.getPokemonDetail(id)
            if (existing != null && existing.chainId.isNotEmpty()) {
                val existingChain = pokemonDao.getEvolutionChain(existing.chainId)
                if (existingChain != null) return // Already fully synced
            }

            val networkDetail = apiService.getPokemonDetail(id)
            val speciesResponse = apiService.getPokemonSpecies(id)
            val flavorText = speciesResponse.flavorTextEntries
                .firstOrNull { it.language.name == "en" }
                ?.flavorText?.replace("\n", " ")?.replace("\u000c", " ") ?: ""
            
            val chainId = speciesResponse.evolutionChain.url.trimEnd('/').split('/').last()
            
            val detailModel = networkDetail.toPokemonDetailModel(flavorText, emptyList(), chainId)
            pokemonDao.insertPokemonDetail(detailModel.toEntity(id, chainId))

            val localChain = pokemonDao.getEvolutionChain(chainId)
            if (localChain == null) {
                syncEvolutionChain(chainId)
            }
        } catch (_: Exception) {
            // Handle error
        }
    }

    override suspend fun syncEvolutionChain(chainId: String) {
        try {
            val url = "https://pokeapi.co/api/v2/evolution-chain/$chainId/"
            val evolutionChainResponse = apiService.getEvolutionChain(url)
            val evolutions = flattenEvolutionChain(evolutionChainResponse.chain)
                .distinctBy { it.id }
                .sortedBy { it.id.toInt() }
            
            pokemonDao.insertEvolutionChain(
                EvolutionChainEntity(id = chainId, evolutions = evolutions)
            )
        } catch (_: Exception) {
            // Handle error
        }
    }

    override suspend fun startEvolutionChainCrawler() {
        var currentId = 1
        var backoffMs = 0L

        while (true) {
            try {
                if (backoffMs > 0) {
                    delay(backoffMs.milliseconds)
                }

                val existing = pokemonDao.getEvolutionChain(currentId.toString())
                if (existing == null) {
                    syncEvolutionChain(currentId.toString())
                    backoffMs = 0
                }
                
                currentId++
                delay(100.milliseconds) 
            } catch (e: HttpException) {
                if (e.code() == 429) {
                    backoffMs = (backoffMs * 2).coerceAtLeast(1000L).coerceAtMost(60000L)
                } else {
                    currentId++
                    backoffMs = 0
                }
            } catch (_: Exception) {
                currentId++
                delay(1000.milliseconds)
            }
            
            if (currentId > 600) break
        }
    }

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
