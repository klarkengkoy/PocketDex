package com.samidevstudio.pocketdex.data

import com.samidevstudio.pocketdex.data.database.TeamDao
import com.samidevstudio.pocketdex.data.database.TeamMemberEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TeamRepositoryTest {

    @Test
    fun addToEmptyTeamUsesFirstSlot() = runBlocking {
        val dao = FakeTeamDao()
        val repository = DefaultTeamRepository(dao)

        assertTrue(repository.addOrReplace("1", "bulbasaur", "", emptyList()))

        assertEquals(listOf(0), dao.members.keys.toList())
    }

    @Test
    fun addToPartiallyFilledTeamPreservesExistingMembers() = runBlocking {
        val dao = FakeTeamDao(member(0, "1", "bulbasaur"), member(2, "3", "venusaur"))
        val repository = DefaultTeamRepository(dao)

        assertTrue(repository.addOrReplace("2", "ivysaur", "", emptyList()))

        assertEquals(listOf(0, 1, 2), dao.members.keys.sorted())
        assertEquals("ivysaur", dao.members[1]?.pokemonName)
        assertEquals("venusaur", dao.members[2]?.pokemonName)
    }

    @Test
    fun removingMiddleSlotKeepsOtherSlotPositions() = runBlocking {
        val dao = FakeTeamDao(member(0, "1", "bulbasaur"), member(1, "2", "ivysaur"), member(2, "3", "venusaur"))
        val repository = DefaultTeamRepository(dao)

        repository.remove(1)

        assertEquals(listOf(0, 2), dao.members.keys.sorted())
    }

    @Test
    fun addAfterMiddleRemovalReusesFirstFreeSlotAndRosterPersists() = runBlocking {
        val dao = FakeTeamDao(member(0, "1", "bulbasaur"), member(1, "2", "ivysaur"), member(2, "3", "venusaur"))
        val repository = DefaultTeamRepository(dao)

        repository.remove(1)
        assertTrue(repository.addOrReplace("4", "charmander", "", emptyList()))

        assertEquals(
            listOf("bulbasaur", "charmander", "venusaur"),
            dao.members.values.sortedBy { it.position }.map { it.pokemonName }
        )
    }

    @Test
    fun fullTeamRejectsAddButAllowsReplacement() = runBlocking {
        val dao = FakeTeamDao(*(0..5).map { member(it, "$it", "pokemon$it") }.toTypedArray())
        val repository = DefaultTeamRepository(dao)

        assertFalse(repository.addOrReplace("6", "mew", "", emptyList()))
        assertTrue(repository.addOrReplace("6", "mew", "", emptyList(), replacePosition = 3))

        assertEquals("mew", dao.members[3]?.pokemonName)
        assertEquals(6, dao.members.size)
    }

    private fun member(position: Int, id: String, name: String) =
        TeamMemberEntity(position, id, name, "", emptyList())

    private class FakeTeamDao(vararg initialMembers: TeamMemberEntity) : TeamDao {
        val members = initialMembers.associateByTo(mutableMapOf()) { it.position }

        override fun getTeamFlow(): Flow<List<TeamMemberEntity>> =
            flowOf(members.values.sortedBy { it.position })

        override suspend fun upsertTeamMember(member: TeamMemberEntity) {
            members[member.position] = member
        }

        override suspend fun removeTeamMember(position: Int) {
            members.remove(position)
        }

        override suspend fun getTeamPositions(): List<Int> = members.keys.toList()
    }
}
