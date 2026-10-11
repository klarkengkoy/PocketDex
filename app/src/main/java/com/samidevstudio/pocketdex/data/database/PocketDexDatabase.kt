package com.samidevstudio.pocketdex.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PokemonEntity::class,
        PokemonDetailEntity::class,
        EvolutionChainEntity::class,
        MoveListEntity::class,
        MoveDetailEntity::class,
        ItemListEntity::class,
        ItemDetailEntity::class,
        TypeEntity::class,
        TeamMemberEntity::class,
        AbilityListEntity::class,
        AbilityDetailEntity::class,
        BerryListEntity::class,
        BerryDetailEntity::class,
        RegionListEntity::class,
        RegionDetailEntity::class,
        LocationListEntity::class,
        LocationDetailEntity::class
    ],
    version = 7,
    exportSchema = true
)
@TypeConverters(PokemonConverters::class)
abstract class PocketDexDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao
    abstract fun moveDao(): MoveDao
    abstract fun itemDao(): ItemDao
    abstract fun typeDao(): TypeDao
    abstract fun teamDao(): TeamDao
    abstract fun abilityDao(): AbilityDao
    abstract fun berryDao(): BerryDao
    abstract fun regionDao(): RegionDao
    abstract fun locationDao(): LocationDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `ability_list` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, PRIMARY KEY(`id`))"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `ability_detail` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `effect` TEXT NOT NULL, `shortEffect` TEXT NOT NULL, `flavorText` TEXT NOT NULL, `pokemonNames` TEXT NOT NULL, PRIMARY KEY(`id`))"
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `berry_list` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, PRIMARY KEY(`id`))"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `berry_detail` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `growthTime` INTEGER NOT NULL, `maxHarvest` INTEGER NOT NULL, `naturalGiftPower` INTEGER NOT NULL, `size` INTEGER NOT NULL, `smoothness` INTEGER NOT NULL, `soilDryness` INTEGER NOT NULL, `firmness` TEXT NOT NULL, `naturalGiftType` TEXT NOT NULL, `flavors` TEXT NOT NULL, PRIMARY KEY(`id`))"
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `region_list` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, PRIMARY KEY(`id`))"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `region_detail` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `mainGeneration` TEXT NOT NULL, `pokedexes` TEXT NOT NULL, `versionGroups` TEXT NOT NULL, `locationNames` TEXT NOT NULL, PRIMARY KEY(`id`))"
                )
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `location_list` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, PRIMARY KEY(`id`))"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `location_detail` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `regionName` TEXT NOT NULL, `areas` TEXT NOT NULL, `gameIndices` TEXT NOT NULL, PRIMARY KEY(`id`))"
                )
            }
        }

        @Volatile
        private var Instance: PocketDexDatabase? = null

        fun getDatabase(context: Context): PocketDexDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, PocketDexDatabase::class.java, "pocketdex_database")
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
