package com.samidevstudio.pocketdex.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TypeDao {
    @Query("SELECT * FROM type_detail ORDER BY name ASC")
    fun getAllTypesFlow(): Flow<List<TypeEntity>>

    @Query("SELECT * FROM type_detail WHERE name = :name")
    fun getTypeFlow(name: String): Flow<TypeEntity?>

    @Query("SELECT * FROM type_detail WHERE name = :name")
    suspend fun getType(name: String): TypeEntity?

    @Query("SELECT COUNT(*) FROM type_detail")
    suspend fun getTypeCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTypes(types: List<TypeEntity>)

    @Query("DELETE FROM type_detail")
    suspend fun clearTypes()
}
