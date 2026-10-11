package com.samidevstudio.pocketdex.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BerryDao {
    @Query("SELECT * FROM berry_list ORDER BY name ASC LIMIT :limit OFFSET :offset")
    fun getBerryListPage(limit: Int, offset: Int): Flow<List<BerryListEntity>>

    @Query("SELECT COUNT(*) FROM berry_list")
    suspend fun getBerryListCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBerryList(berries: List<BerryListEntity>)

    @Query("SELECT * FROM berry_detail WHERE id = :id OR name = :id")
    fun getBerryDetailFlow(id: String): Flow<BerryDetailEntity?>

    @Query("SELECT * FROM berry_detail WHERE id = :id OR name = :id")
    suspend fun getBerryDetail(id: String): BerryDetailEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBerryDetail(berry: BerryDetailEntity)

    @Query("DELETE FROM berry_list")
    suspend fun clearBerryList()

    @Query("DELETE FROM berry_detail")
    suspend fun clearBerryDetail()
}
