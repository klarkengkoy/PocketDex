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
        EvolutionChainEntity::class,
        MoveListEntity::class,
        MoveDetailEntity::class,
        ItemListEntity::class,
        ItemDetailEntity::class,
        TypeEntity::class,
        TeamMemberEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(PokemonConverters::class)
abstract class PocketDexDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao
    abstract fun moveDao(): MoveDao
    abstract fun itemDao(): ItemDao
    abstract fun typeDao(): TypeDao
    abstract fun teamDao(): TeamDao

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
