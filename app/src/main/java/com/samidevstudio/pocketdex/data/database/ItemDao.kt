package com.samidevstudio.pocketdex.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM item_list ORDER BY name ASC LIMIT :limit OFFSET :offset")
    fun getItemListPage(limit: Int, offset: Int): Flow<List<ItemListEntity>>

    @Query("SELECT COUNT(*) FROM item_list")
    suspend fun getItemListCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItemList(items: List<ItemListEntity>)

    @Query("SELECT * FROM item_detail WHERE id = :id")
    fun getItemDetailFlow(id: String): Flow<ItemDetailEntity?>

    @Query("SELECT * FROM item_detail WHERE id = :id")
    suspend fun getItemDetail(id: String): ItemDetailEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItemDetail(item: ItemDetailEntity)

    @Query("UPDATE item_list SET category = :category WHERE id = :id")
    suspend fun updateItemCategory(id: String, category: String)

    @Query("DELETE FROM item_list")
    suspend fun clearItemList()

    @Query("DELETE FROM item_detail")
    suspend fun clearItemDetail()
}
