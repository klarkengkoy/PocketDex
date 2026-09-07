package com.samidevstudio.pocketdex.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamDao {
    @Query("SELECT * FROM team_members ORDER BY position ASC")
    fun getTeamFlow(): Flow<List<TeamMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTeamMember(member: TeamMemberEntity)

    @Query("DELETE FROM team_members WHERE position = :position")
    suspend fun removeTeamMember(position: Int)

    @Query("SELECT COUNT(*) FROM team_members")
    suspend fun getTeamSize(): Int
}
