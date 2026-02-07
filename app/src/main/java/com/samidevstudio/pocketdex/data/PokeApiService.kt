package com.samidevstudio.pocketdex.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * This interface defines how we talk to the PokéAPI.
 */
interface PokeApiService {

    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): PokemonResponse

    /**
     * Fetches detailed information for a single Pokémon.
     */
    @GET("pokemon/{id}")
    suspend fun getPokemonDetail(
        @Path("id") id: String
    ): PokemonDetail

    /**
     * Fetches the species information, including flavor text and evolution chain links.
     */
    @GET("pokemon-species/{id}")
    suspend fun getPokemonSpecies(
        @Path("id") id: String
    ): PokemonSpeciesResponse

    /**
     * Fetches the evolution chain from a full URL provided by the species response.
     */
    @GET
    suspend fun getEvolutionChain(
        @Url url: String
    ): EvolutionChainResponse
}
