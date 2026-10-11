package com.samidevstudio.pocketdex

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.samidevstudio.pocketdex.data.database.PocketDexDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        PocketDexDatabase::class.java,
        listOf(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate3To4PreservesTeamRows() = runBlocking {
        val dbName = "migration-test-3-4"
        InstrumentationRegistry.getInstrumentation().targetContext.deleteDatabase(dbName)

        var db = helper.createDatabase(dbName, 3)
        db.execSQL(
            "INSERT INTO team_members (position, pokemonId, pokemonName, imageUrl, types) VALUES (0, '25', 'pikachu', 'http://img', '[\"Electric\"]')"
        )
        db.close()

        db = helper.runMigrationsAndValidate(dbName, 4, true, PocketDexDatabase.MIGRATION_3_4)
        db.close()

        val migratedDb = Room.databaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            PocketDexDatabase::class.java,
            dbName
        ).addMigrations(
            PocketDexDatabase.MIGRATION_3_4,
            PocketDexDatabase.MIGRATION_4_5,
            PocketDexDatabase.MIGRATION_5_6,
            PocketDexDatabase.MIGRATION_6_7
        ).build()

        try {
            val team = migratedDb.teamDao().getTeamFlow().first()
            assertEquals(1, team.size)
            assertEquals("pikachu", team[0].pokemonName)
        } finally {
            migratedDb.close()
            ApplicationProvider.getApplicationContext<Context>().deleteDatabase(dbName)
        }
    }

    @Test
    fun migrate4To5PreservesTeamAndResourceRows() = runBlocking {
        val dbName = "migration-test-4-5"
        InstrumentationRegistry.getInstrumentation().targetContext.deleteDatabase(dbName)

        var db = helper.createDatabase(dbName, 4)
        db.execSQL(
            "INSERT INTO team_members (position, pokemonId, pokemonName, imageUrl, types) VALUES (0, '25', 'pikachu', 'http://img', '[\"Electric\"]')"
        )
        db.execSQL(
            "INSERT INTO ability_list (id, name) VALUES ('1', 'stench')"
        )
        db.close()

        db = helper.runMigrationsAndValidate(dbName, 5, true, PocketDexDatabase.MIGRATION_4_5)
        db.close()

        val migratedDb = Room.databaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            PocketDexDatabase::class.java,
            dbName
        ).addMigrations(
            PocketDexDatabase.MIGRATION_3_4,
            PocketDexDatabase.MIGRATION_4_5,
            PocketDexDatabase.MIGRATION_5_6,
            PocketDexDatabase.MIGRATION_6_7
        ).build()

        try {
            val team = migratedDb.teamDao().getTeamFlow().first()
            assertEquals(1, team.size)
            assertEquals("pikachu", team[0].pokemonName)

            val abilityList = migratedDb.abilityDao().getAbilityListPage(10, 0).first()
            assertEquals(1, abilityList.size)
            assertEquals("stench", abilityList[0].name)
        } finally {
            migratedDb.close()
            ApplicationProvider.getApplicationContext<Context>().deleteDatabase(dbName)
        }
    }

    @Test
    fun migrate5To6PreservesTeamAndResourceRows() = runBlocking {
        val dbName = "migration-test-5-6"
        InstrumentationRegistry.getInstrumentation().targetContext.deleteDatabase(dbName)

        var db = helper.createDatabase(dbName, 5)
        db.execSQL(
            "INSERT INTO team_members (position, pokemonId, pokemonName, imageUrl, types) VALUES (0, '25', 'pikachu', 'http://img', '[\"Electric\"]')"
        )
        db.execSQL(
            "INSERT INTO berry_list (id, name) VALUES ('1', 'cheri')"
        )
        db.close()

        db = helper.runMigrationsAndValidate(dbName, 6, true, PocketDexDatabase.MIGRATION_5_6)
        db.close()

        val migratedDb = Room.databaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            PocketDexDatabase::class.java,
            dbName
        ).addMigrations(
            PocketDexDatabase.MIGRATION_3_4,
            PocketDexDatabase.MIGRATION_4_5,
            PocketDexDatabase.MIGRATION_5_6,
            PocketDexDatabase.MIGRATION_6_7
        ).build()

        try {
            val team = migratedDb.teamDao().getTeamFlow().first()
            assertEquals(1, team.size)
            assertEquals("pikachu", team[0].pokemonName)

            val berryList = migratedDb.berryDao().getBerryListPage(10, 0).first()
            assertEquals(1, berryList.size)
            assertEquals("cheri", berryList[0].name)
        } finally {
            migratedDb.close()
            ApplicationProvider.getApplicationContext<Context>().deleteDatabase(dbName)
        }
    }

    @Test
    fun migrate6To7PreservesTeamAndResourceRows() = runBlocking {
        val dbName = "migration-test-6-7"
        InstrumentationRegistry.getInstrumentation().targetContext.deleteDatabase(dbName)

        var db = helper.createDatabase(dbName, 6)
        db.execSQL(
            "INSERT INTO team_members (position, pokemonId, pokemonName, imageUrl, types) VALUES (0, '25', 'pikachu', 'http://img', '[\"Electric\"]')"
        )
        db.execSQL(
            "INSERT INTO region_list (id, name) VALUES ('1', 'kanto')"
        )
        db.close()

        db = helper.runMigrationsAndValidate(dbName, 7, true, PocketDexDatabase.MIGRATION_6_7)
        db.close()

        val migratedDb = Room.databaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            PocketDexDatabase::class.java,
            dbName
        ).addMigrations(
            PocketDexDatabase.MIGRATION_3_4,
            PocketDexDatabase.MIGRATION_4_5,
            PocketDexDatabase.MIGRATION_5_6,
            PocketDexDatabase.MIGRATION_6_7
        ).build()

        try {
            val team = migratedDb.teamDao().getTeamFlow().first()
            assertEquals(1, team.size)
            assertEquals("pikachu", team[0].pokemonName)

            val regionList = migratedDb.regionDao().getRegionListPage(10, 0).first()
            assertEquals(1, regionList.size)
            assertEquals("kanto", regionList[0].name)
        } finally {
            migratedDb.close()
            ApplicationProvider.getApplicationContext<Context>().deleteDatabase(dbName)
        }
    }
}
