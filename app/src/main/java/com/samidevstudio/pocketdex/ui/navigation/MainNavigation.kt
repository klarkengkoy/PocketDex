package com.samidevstudio.pocketdex.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.samidevstudio.pocketdex.ui.PokemonDetailScreen
import com.samidevstudio.pocketdex.ui.PokemonScreen
import com.samidevstudio.pocketdex.ui.PokemonViewModel
import kotlinx.serialization.Serializable

/**
 * PokedexRoute defines the available screens in our app.
 */
@Serializable
sealed interface PokedexRoute {
    @Serializable
    data object List : PokedexRoute

    @Serializable
    data class Detail(
        val pokemonId: String,
        val pokemonName: String
    ) : PokedexRoute
}

/**
 * MainNavigation handles the app's screen flow.
 * Focused purely on stable navigation logic.
 */
@Composable
fun MainNavigation(
    backStack: SnapshotStateList<Any>,
    onBack: () -> Unit
) {
    val pokemonViewModel: PokemonViewModel = viewModel()

    NavDisplay(
        backStack = backStack,
        onBack = onBack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = {
            ContentTransform(
                targetContentEnter = fadeIn(),
                initialContentExit = fadeOut()
            )
        },
        entryProvider = entryProvider {
            entry<PokedexRoute.List> {
                PokemonScreen(
                    viewModel = pokemonViewModel,
                    onPokemonClick = { pokemon ->
                        backStack.add(PokedexRoute.Detail(pokemon.id, pokemon.name))
                    }
                )
            }
            
            entry<PokedexRoute.Detail> { route ->
                PokemonDetailScreen(
                    pokemonId = route.pokemonId,
                    pokemonName = route.pokemonName,
                    viewModel = pokemonViewModel,
                    onBack = onBack
                )
            }
        }
    )
}
