package com.samidevstudio.pocketdex

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import android.content.Context
import com.samidevstudio.pocketdex.data.database.PocketDexDatabase
import com.samidevstudio.pocketdex.data.database.TeamMemberEntity
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Test

class TeamDatabasePersistenceTest {

    @Test
    fun savedTeamSurvivesDatabaseReopen() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val databaseName = "team-persistence-test"
        context.deleteDatabase(databaseName)

        val firstDatabase = Room.databaseBuilder(context, PocketDexDatabase::class.java, databaseName).build()
        firstDatabase.teamDao().upsertTeamMember(
            TeamMemberEntity(2, "25", "pikachu", "image", listOf("Electric"))
        )
        firstDatabase.close()

        val reopenedDatabase = Room.databaseBuilder(context, PocketDexDatabase::class.java, databaseName).build()
        try {
            assertEquals(
                listOf(TeamMemberEntity(2, "25", "pikachu", "image", listOf("Electric"))),
                reopenedDatabase.teamDao().getTeamFlow().first()
            )
        } finally {
            reopenedDatabase.close()
            context.deleteDatabase(databaseName)
        }
    }
}
