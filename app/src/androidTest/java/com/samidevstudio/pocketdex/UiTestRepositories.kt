package com.samidevstudio.pocketdex

import com.samidevstudio.pocketdex.data.AbilityRepository
import com.samidevstudio.pocketdex.data.BerryRepository
import com.samidevstudio.pocketdex.data.DefaultAbilityRepository
import com.samidevstudio.pocketdex.data.DefaultBerryRepository
import com.samidevstudio.pocketdex.data.DefaultItemRepository
import com.samidevstudio.pocketdex.data.DefaultLocationRepository
import com.samidevstudio.pocketdex.data.DefaultMoveRepository
import com.samidevstudio.pocketdex.data.DefaultRegionRepository
import com.samidevstudio.pocketdex.data.ItemRepository
import com.samidevstudio.pocketdex.data.LocationRepository
import com.samidevstudio.pocketdex.data.MoveRepository
import com.samidevstudio.pocketdex.data.PokemonRepository
import com.samidevstudio.pocketdex.data.RegionRepository
import com.samidevstudio.pocketdex.data.TeamRepository
import com.samidevstudio.pocketdex.data.TypeRepository
import com.samidevstudio.pocketdex.domain.TypeRelations
import com.samidevstudio.pocketdex.ui.pokemon.EvolutionNode
import com.samidevstudio.pocketdex.ui.pokemon.PokemonDetailModel
import com.samidevstudio.pocketdex.ui.pokemon.PokemonUiModel
import com.samidevstudio.pocketdex.ui.pokemon.StatInfo
import com.samidevstudio.pocketdex.ui.team.TeamMemberModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.flow.flowOf

/** Included only by the two UI test classes; persistence tests retain production bindings. */
@Module
@dagger.hilt.migration.DisableInstallInCheck
abstract class UiTestRepositories {
    @Binds abstract fun moves(repository: DefaultMoveRepository): MoveRepository
    @Binds abstract fun items(repository: DefaultItemRepository): ItemRepository
    @Binds abstract fun abilities(repository: DefaultAbilityRepository): AbilityRepository
    @Binds abstract fun berries(repository: DefaultBerryRepository): BerryRepository
    @Binds abstract fun regions(repository: DefaultRegionRepository): RegionRepository
    @Binds abstract fun locations(repository: DefaultLocationRepository): LocationRepository

    companion object {
        // Empty image URLs prevent sprite HTTP requests. These tests verify semantics/navigation.
        private val bulbasaur = PokemonUiModel("1", "bulbasaur", "", listOf("grass", "poison"))
        private val detail = PokemonDetailModel(
            id = "1", name = "bulbasaur", imageUrl = "", types = bulbasaur.types,
            height = 7, weight = 69,
            stats = listOf(StatInfo("HP", 45), StatInfo("Attack", 49), StatInfo("Defense", 49)),
            flavorText = "A seed Pokemon.",
            chainId = "1"
        )

        @Provides
        fun pokemon(): PokemonRepository = object : PokemonRepository {
            override fun getPokemonListFlow() = flowOf(listOf(bulbasaur))
            override suspend fun fetchPokemonList(offset: Int, limit: Int) = Unit
            override fun getPokemonDetailFlow(id: String) = flowOf(detail.takeIf { id == "1" })
            override fun getEvolutionChainFlow(chainId: String) =
                flowOf(listOf(EvolutionNode("1", "bulbasaur", "")))
            override suspend fun syncPokemonDetail(id: String) = Unit
            override suspend fun syncEvolutionChain(chainId: String) = Unit
            override suspend fun backfillMissingTypes() = Unit
        }

        @Provides
        fun types(): TypeRepository = object : TypeRepository {
            override fun getAllTypesFlow() = flowOf(emptyList<TypeRelations>())
            override fun getTypeFlow(name: String) = flowOf<TypeRelations?>(null)
            override suspend fun syncAllTypes() = Unit
        }

        @Provides
        fun team(): TeamRepository = object : TeamRepository {
            override fun getTeamFlow() = flowOf(emptyList<TeamMemberModel>())
            override suspend fun addOrReplace(
                pokemonId: String, pokemonName: String, imageUrl: String,
                types: List<String>, replacePosition: Int?
            ): Boolean = error("Team mutations are outside these UI tests")
            override suspend fun remove(position: Int): Unit =
                error("Team mutations are outside these UI tests")
        }
    }
}
