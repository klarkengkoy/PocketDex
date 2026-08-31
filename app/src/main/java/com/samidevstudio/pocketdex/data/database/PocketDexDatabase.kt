package com.samidevstudio.pocketdex.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        PokemonEntity::class, 
        PokemonDetailEntity::class, 
        EvolutionChainEntity::class
    ], 
    version = 2, 
    exportSchema = false
)
@TypeConverters(PokemonConverters::class)
abstract class PocketDexDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao

    companion object {
        @Volatile
        private var Instance: PocketDexDatabase? = null

        fun getDatabase(context: Context): PocketDexDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, PocketDexDatabase::class.java, "pocketdex_database")
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
