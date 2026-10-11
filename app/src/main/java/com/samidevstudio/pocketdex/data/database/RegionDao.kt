package com.samidevstudio.pocketdex.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RegionDao {
    @Query("SELECT * FROM region_list ORDER BY id ASC LIMIT :limit OFFSET :offset")
    fun getRegionListPage(limit: Int, offset: Int): Flow<List<RegionListEntity>>

    @Query("SELECT COUNT(*) FROM region_list")
    suspend fun getRegionListCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegionList(regions: List<RegionListEntity>)

    @Query("SELECT * FROM region_detail WHERE id = :id OR name = :id")
    fun getRegionDetailFlow(id: String): Flow<RegionDetailEntity?>

    @Query("SELECT * FROM region_detail WHERE id = :id OR name = :id")
    suspend fun getRegionDetail(id: String): RegionDetailEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegionDetail(region: RegionDetailEntity)

    @Query("DELETE FROM region_list")
    suspend fun clearRegionList()

    @Query("DELETE FROM region_detail")
    suspend fun clearRegionDetail()
}
