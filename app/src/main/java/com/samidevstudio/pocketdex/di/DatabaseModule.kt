package com.samidevstudio.pocketdex.di

import android.content.Context
import com.samidevstudio.pocketdex.data.database.ItemDao
import com.samidevstudio.pocketdex.data.database.MoveDao
import com.samidevstudio.pocketdex.data.database.PocketDexDatabase
import com.samidevstudio.pocketdex.data.database.PokemonDao
import com.samidevstudio.pocketdex.data.database.TeamDao
import com.samidevstudio.pocketdex.data.database.TypeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PocketDexDatabase {
        return PocketDexDatabase.getDatabase(context)
    }

    @Provides
    fun providePokemonDao(database: PocketDexDatabase): PokemonDao = database.pokemonDao()

    @Provides
    fun provideMoveDao(database: PocketDexDatabase): MoveDao = database.moveDao()

    @Provides
    fun provideItemDao(database: PocketDexDatabase): ItemDao = database.itemDao()

    @Provides
    fun provideTypeDao(database: PocketDexDatabase): TypeDao = database.typeDao()

    @Provides
    fun provideTeamDao(database: PocketDexDatabase): TeamDao = database.teamDao()
}
