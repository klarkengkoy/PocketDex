package com.samidevstudio.pocketdex.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MoveDao {
    @Query("SELECT * FROM move_list ORDER BY name ASC LIMIT :limit OFFSET :offset")
    fun getMoveListPage(limit: Int, offset: Int): Flow<List<MoveListEntity>>

    @Query("SELECT COUNT(*) FROM move_list")
    suspend fun getMoveListCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoveList(moves: List<MoveListEntity>)

    @Query("SELECT * FROM move_detail WHERE id = :id")
    fun getMoveDetailFlow(id: String): Flow<MoveDetailEntity?>

    @Query("SELECT * FROM move_detail WHERE id = :id")
    suspend fun getMoveDetail(id: String): MoveDetailEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoveDetail(move: MoveDetailEntity)

    @Query("DELETE FROM move_list")
    suspend fun clearMoveList()

    @Query("DELETE FROM move_detail")
    suspend fun clearMoveDetail()
}
