package com.samidevstudio.pocketdex.data

import com.samidevstudio.pocketdex.data.database.ItemDao
import com.samidevstudio.pocketdex.data.database.ItemDetailEntity
import com.samidevstudio.pocketdex.data.database.ItemListEntity
import com.samidevstudio.pocketdex.ui.items.ItemDetailModel
import com.samidevstudio.pocketdex.ui.items.ItemListModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface ItemRepository {
    fun getItemListFlow(limit: Int, offset: Int): Flow<List<ItemListModel>>
    suspend fun fetchItemList(offset: Int, limit: Int = 60)

    fun getItemDetailFlow(id: String): Flow<ItemDetailModel?>
    suspend fun syncItemDetail(id: String)
}

class DefaultItemRepository @Inject constructor(
    private val apiService: PokeApiService,
    private val itemDao: ItemDao
) : ItemRepository {

    private val inFlightListFetches = mutableSetOf<Pair<Int, Int>>()
    private val inFlightDetailSyncs = mutableSetOf<String>()

    override fun getItemListFlow(limit: Int, offset: Int): Flow<List<ItemListModel>> =
        itemDao.getItemListPage(limit, offset).map { list ->
            list.map { ItemListModel(id = it.id, name = it.name, category = it.category) }
        }

    override suspend fun fetchItemList(offset: Int, limit: Int) {
        val fetchKey = offset to limit
        if (inFlightListFetches.contains(fetchKey)) return
        inFlightListFetches.add(fetchKey)

        try {
            val existingCount = itemDao.getItemListCount()
            if (existingCount >= offset + limit) return

            val response = apiService.getItemList(offset = offset, limit = limit)
            if (response.results.isEmpty()) return

            itemDao.insertItemList(
                response.results.map { ItemListEntity(id = it.id, name = it.name) }
            )
        } catch (_: Exception) {
            // List stays at whatever was cached; user can pull to retry via loadMore.
        } finally {
            inFlightListFetches.remove(fetchKey)
        }
    }

    override fun getItemDetailFlow(id: String): Flow<ItemDetailModel?> =
        itemDao.getItemDetailFlow(id).map { entity ->
            entity?.let {
                ItemDetailModel(
                    id = it.id,
                    name = it.name,
                    cost = it.cost,
                    category = it.category,
                    spriteUrl = it.spriteUrl,
                    effect = it.effect,
                    flavorText = it.flavorText
                )
            }
        }

    override suspend fun syncItemDetail(id: String) {
        if (inFlightDetailSyncs.contains(id)) return
        inFlightDetailSyncs.add(id)

        try {
            val existing = itemDao.getItemDetail(id)
            if (existing != null) return

            val detail = apiService.getItemDetail(id)
            val effectEntry = detail.effectEntries.firstOrNull { it.language.name == "en" }
            val flavorEntry = detail.flavorTextEntries.firstOrNull { it.language.name == "en" }

            itemDao.insertItemDetail(
                ItemDetailEntity(
                    id = detail.id.toString(),
                    name = detail.name,
                    cost = detail.cost,
                    category = detail.category.name,
                    spriteUrl = detail.sprites.default,
                    effect = (effectEntry?.shortEffect ?: effectEntry?.effect ?: "").replace("\n", " "),
                    flavorText = (flavorEntry?.text ?: "").replace("\n", " ").replace("\u000c", " ")
                )
            )
            itemDao.updateItemCategory(id, detail.category.name)
        } catch (_: Exception) {
            // Handled by leaving the Flow null; the ViewModel keeps showing loading/retry.
        } finally {
            inFlightDetailSyncs.remove(id)
        }
    }
}
