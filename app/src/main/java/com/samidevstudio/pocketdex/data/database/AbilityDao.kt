package com.samidevstudio.pocketdex.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AbilityDao {
    @Query("SELECT * FROM ability_list ORDER BY name ASC LIMIT :limit OFFSET :offset")
    fun getAbilityListPage(limit: Int, offset: Int): Flow<List<AbilityListEntity>>

    @Query("SELECT COUNT(*) FROM ability_list")
    suspend fun getAbilityListCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAbilityList(abilities: List<AbilityListEntity>)

    @Query("SELECT * FROM ability_detail WHERE id = :id OR name = :id")
    fun getAbilityDetailFlow(id: String): Flow<AbilityDetailEntity?>

    @Query("SELECT * FROM ability_detail WHERE id = :id OR name = :id")
    suspend fun getAbilityDetail(id: String): AbilityDetailEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAbilityDetail(ability: AbilityDetailEntity)

    @Query("DELETE FROM ability_list")
    suspend fun clearAbilityList()

    @Query("DELETE FROM ability_detail")
    suspend fun clearAbilityDetail()
}
