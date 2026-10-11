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

    @GET("move")
    suspend fun getMoveList(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): MoveListResponse

    @GET("move/{id}")
    suspend fun getMoveDetail(
        @Path("id") id: String
    ): MoveDetail

    @GET("type")
    suspend fun getTypeList(): TypeListResponse

    @GET("type/{id}")
    suspend fun getTypeDetail(
        @Path("id") id: String
    ): TypeDetail

    @GET("item")
    suspend fun getItemList(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): ItemListResponse

    @GET("item/{id}")
    suspend fun getItemDetail(
        @Path("id") id: String
    ): ItemDetail

    @GET("ability")
    suspend fun getAbilityList(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): AbilityListResponse

    @GET("ability/{id}")
    suspend fun getAbilityDetail(
        @Path("id") id: String
    ): AbilityDetail

    @GET("berry")
    suspend fun getBerryList(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): BerryListResponse

    @GET("berry/{id}")
    suspend fun getBerryDetail(
        @Path("id") id: String
    ): BerryDetail

    @GET("region")
    suspend fun getRegionList(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): RegionListResponse

    @GET("region/{id}")
    suspend fun getRegionDetail(
        @Path("id") id: String
    ): RegionDetail

    @GET("location")
    suspend fun getLocationList(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): LocationListResponse

    @GET("location/{id}")
    suspend fun getLocationDetail(
        @Path("id") id: String
    ): LocationDetail
}
