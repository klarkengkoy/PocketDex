package com.samidevstudio.pocketdex.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonDao {
    @Query("SELECT * FROM pokemon ORDER BY CAST(id AS INTEGER) ASC LIMIT :limit OFFSET :offset")
    fun getPokemonList(limit: Int, offset: Int): Flow<List<PokemonEntity>>

    @Query("SELECT COUNT(*) FROM pokemon WHERE CAST(id AS INTEGER) > :offset AND CAST(id AS INTEGER) <= :offset + :limit")
    suspend fun getPokemonCountInRange(offset: Int, limit: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemonList(pokemon: List<PokemonEntity>)

    @Query("SELECT * FROM pokemon_detail WHERE id = :id")
    suspend fun getPokemonDetail(id: String): PokemonDetailEntity?

    @Query("SELECT * FROM pokemon_detail WHERE id = :id")
    fun getPokemonDetailFlow(id: String): Flow<PokemonDetailEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemonDetail(pokemonDetail: PokemonDetailEntity)

    @Query("UPDATE pokemon SET types = :types WHERE id = :id")
    suspend fun updatePokemonTypes(id: String, types: List<String>)

    @Query("SELECT id FROM pokemon WHERE types = '[]' ORDER BY CAST(id AS INTEGER) ASC LIMIT 100")
    suspend fun getPokemonIdsMissingTypes(): List<String>

    @Query("SELECT * FROM evolution_chains WHERE id = :id")
    suspend fun getEvolutionChain(id: String): EvolutionChainEntity?

    @Query("SELECT * FROM evolution_chains WHERE id = :id")
    fun getEvolutionChainFlow(id: String): Flow<EvolutionChainEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvolutionChain(chain: EvolutionChainEntity)

    @Query("DELETE FROM pokemon")
    suspend fun clearPokemonList()

    @Query("DELETE FROM pokemon_detail")
    suspend fun clearPokemonDetail()

    @Query("DELETE FROM evolution_chains")
    suspend fun clearEvolutionChains()
}
