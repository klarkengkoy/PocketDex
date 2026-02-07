package com.samidevstudio.pocketdex.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import com.samidevstudio.pocketdex.ui.PokemonDetailScreen
import com.samidevstudio.pocketdex.ui.PokemonScreen
import com.samidevstudio.pocketdex.ui.PokemonViewModel
import kotlinx.serialization.Serializable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * PokedexRoute defines the available screens in our app.
 * Expanded to support the 5-tab Pokeball Hub.
 */
@Serializable
sealed interface PokedexRoute {
    @Serializable
    data object List : PokedexRoute

    @Serializable
    data object Items : PokedexRoute

    @Serializable
    data object Moves : PokedexRoute

    @Serializable
    data object Strategy : PokedexRoute

    @Serializable
    data object Options : PokedexRoute

    @Serializable
    data class Detail(
        val pokemonId: String,
        val pokemonName: String
    ) : PokedexRoute
}

/**
 * MainNavigation handles the app's screen flow.
 * Wrapped in SharedTransitionLayout to enable sprite "flight" animations.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MainNavigation(
    backStack: SnapshotStateList<Any>,
    onBack: () -> Unit
) {
    val pokemonViewModel: PokemonViewModel = viewModel()

    SharedTransitionLayout {
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
                    val animatedVisibilityScope = LocalNavAnimatedContentScope.current
                        
                    PokemonScreen(
                        viewModel = pokemonViewModel,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = animatedVisibilityScope,
                        onPokemonClick = { pokemon ->
                            backStack.add(PokedexRoute.Detail(pokemon.id, pokemon.name))
                        }
                    )
                }

                // Placeholder screens for the new Hub tabs
                entry<PokedexRoute.Items> { PlaceholderScreen("THE BAG (ITEMS)") }
                entry<PokedexRoute.Moves> { PlaceholderScreen("MOVE-DEX") }
                entry<PokedexRoute.Strategy> { PlaceholderScreen("STRATEGY (TYPES)") }
                entry<PokedexRoute.Options> { PlaceholderScreen("OPTIONS") }
                
                entry<PokedexRoute.Detail> { route ->
                    val animatedVisibilityScope = LocalNavAnimatedContentScope.current

                    PokemonDetailScreen(
                        pokemonId = route.pokemonId,
                        pokemonName = route.pokemonName,
                        viewModel = pokemonViewModel,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = animatedVisibilityScope,
                        onBack = onBack
                    )
                }
            }
        )
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = name)
    }
}
