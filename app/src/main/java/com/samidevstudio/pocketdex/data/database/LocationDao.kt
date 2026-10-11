package com.samidevstudio.pocketdex.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Query("SELECT * FROM location_list ORDER BY name ASC LIMIT :limit OFFSET :offset")
    fun getLocationListPage(limit: Int, offset: Int): Flow<List<LocationListEntity>>

    @Query("SELECT COUNT(*) FROM location_list")
    suspend fun getLocationListCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationList(locations: List<LocationListEntity>)

    @Query("SELECT * FROM location_detail WHERE id = :id OR name = :id")
    fun getLocationDetailFlow(id: String): Flow<LocationDetailEntity?>

    @Query("SELECT * FROM location_detail WHERE id = :id OR name = :id")
    suspend fun getLocationDetail(id: String): LocationDetailEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationDetail(location: LocationDetailEntity)

    @Query("DELETE FROM location_list")
    suspend fun clearLocationList()

    @Query("DELETE FROM location_detail")
    suspend fun clearLocationDetail()
}
