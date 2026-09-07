package com.samidevstudio.pocketdex.data

import android.content.Context
import com.samidevstudio.pocketdex.data.database.PocketDexDatabase

interface AppContainer {
    val pokemonRepository: PokemonRepository
    val moveRepository: MoveRepository
    val itemRepository: ItemRepository
    val typeRepository: TypeRepository
    val teamRepository: TeamRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val database by lazy { PocketDexDatabase.getDatabase(context) }

    override val pokemonRepository: PokemonRepository by lazy {
        DefaultPokemonRepository(
            apiService = RetrofitClient.pokeApiService,
            pokemonDao = database.pokemonDao()
        )
    }

    override val moveRepository: MoveRepository by lazy {
        DefaultMoveRepository(moveDao = database.moveDao())
    }

    override val itemRepository: ItemRepository by lazy {
        DefaultItemRepository(itemDao = database.itemDao())
    }

    override val typeRepository: TypeRepository by lazy {
        DefaultTypeRepository(typeDao = database.typeDao())
    }

    override val teamRepository: TeamRepository by lazy {
        DefaultTeamRepository(teamDao = database.teamDao())
    }
}
