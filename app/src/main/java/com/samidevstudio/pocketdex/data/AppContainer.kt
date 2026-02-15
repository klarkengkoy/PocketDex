package com.samidevstudio.pocketdex.data

interface AppContainer {
    val pokemonRepository: PokemonRepository
}

class DefaultAppContainer : AppContainer {

    override val pokemonRepository: PokemonRepository by lazy {
        DefaultPokemonRepository(apiService = RetrofitClient.pokeApiService)
    }
}
