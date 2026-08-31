package com.samidevstudio.pocketdex.data

import android.content.Context
import com.samidevstudio.pocketdex.data.database.PocketDexDatabase

interface AppContainer {
    val pokemonRepository: PokemonRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val pokemonRepository: PokemonRepository by lazy {
        DefaultPokemonRepository(
            apiService = RetrofitClient.pokeApiService,
            pokemonDao = PocketDexDatabase.getDatabase(context).pokemonDao()
        )
    }
}
