package com.samidevstudio.pocketdex.di

import com.samidevstudio.pocketdex.data.DefaultItemRepository
import com.samidevstudio.pocketdex.data.DefaultMoveRepository
import com.samidevstudio.pocketdex.data.DefaultPokemonRepository
import com.samidevstudio.pocketdex.data.DefaultTeamRepository
import com.samidevstudio.pocketdex.data.DefaultTypeRepository
import com.samidevstudio.pocketdex.data.ItemRepository
import com.samidevstudio.pocketdex.data.MoveRepository
import com.samidevstudio.pocketdex.data.PokemonRepository
import com.samidevstudio.pocketdex.data.TeamRepository
import com.samidevstudio.pocketdex.data.TypeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPokemonRepository(
        pokemonRepository: DefaultPokemonRepository
    ): PokemonRepository

    @Binds
    @Singleton
    abstract fun bindMoveRepository(
        moveRepository: DefaultMoveRepository
    ): MoveRepository

    @Binds
    @Singleton
    abstract fun bindItemRepository(
        itemRepository: DefaultItemRepository
    ): ItemRepository

    @Binds
    @Singleton
    abstract fun bindTypeRepository(
        typeRepository: DefaultTypeRepository
    ): TypeRepository

    @Binds
    @Singleton
    abstract fun bindTeamRepository(
        teamRepository: DefaultTeamRepository
    ): TeamRepository
}
