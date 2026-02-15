package com.samidevstudio.pocketdex.ui.navigation

import android.os.Parcelable
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.ArcMode
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import com.samidevstudio.pocketdex.ui.options.OptionsScreen
import com.samidevstudio.pocketdex.ui.options.OptionsViewModel
import com.samidevstudio.pocketdex.ui.pokemon.PokemonDetailScreen
import com.samidevstudio.pocketdex.ui.pokemon.PokemonScreen
import com.samidevstudio.pocketdex.ui.pokemon.PokemonViewModel
import kotlinx.serialization.Serializable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.parcelize.Parcelize

/**
 * Pokedex Animation Standards.
 */
const val POKEDEX_ANIM_MS = 2000

// Used for screen slides - starts fast, settles smoothly
val PokedexSettlingCurve = CubicBezierEasing(0.18f, 0.82f, 0.23f, 1.0f)

// Used for Pokémon sprites - smooth flight without overshoot
val SpriteFlightCurve = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)

/**
 * Creates a flight path for Pokémon sprites.
 * Automatically chooses an Arc direction based on the starting position.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
fun pokemonSpriteTransform(duration: Int = POKEDEX_ANIM_MS): BoundsTransform =
    BoundsTransform { initial, target ->
        // Logic: If the card is in the lower half of the screen, arc downwards.
        // We use 800 as a rough threshold for "lower half" on modern devices.
        val arcDirection = if (initial.top > 800) ArcMode.ArcBelow else ArcMode.ArcAbove

        keyframes {
            this.durationMillis = duration
            initial at 0 using arcDirection using SpriteFlightCurve
            target at duration
        }
    }

/**
 * Route definitions for the app.
 * Using @Parcelize allows the navigation state to survive process death.
 */
@Serializable
sealed interface PokedexRoute : NavKey, Parcelable {
    @Serializable
    @Parcelize
    data object List : PokedexRoute
    
    @Serializable
    @Parcelize
    data object Items : PokedexRoute
    
    @Serializable
    @Parcelize
    data object Moves : PokedexRoute
    
    @Serializable
    @Parcelize
    data object Strategy : PokedexRoute
    
    @Serializable
    @Parcelize
    data object Options : PokedexRoute
    
    @Serializable
    @Parcelize
    data class Detail(val pokemonId: String, val pokemonName: String) : PokedexRoute
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MainNavigation(
    backStack: NavBackStack<NavKey>,
    optionsViewModel: OptionsViewModel,
    onBack: () -> Unit
) {
    // FIX: Pass the Factory to ensure the ViewModel is created with its required repository.
    val pokemonViewModel: PokemonViewModel = viewModel(factory = PokemonViewModel.Factory)

    SharedTransitionLayout {
        NavDisplay(
            backStack = backStack,
            onBack = onBack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            transitionSpec = {
                val forward = initialState.key is PokedexRoute.List && targetState.key is PokedexRoute.Detail
                val backward = initialState.key is PokedexRoute.Detail && targetState.key is PokedexRoute.List

                when {
                    forward -> {
                        (slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(POKEDEX_ANIM_MS, easing = PokedexSettlingCurve)
                        ) + fadeIn(tween(POKEDEX_ANIM_MS)) + scaleIn(
                            initialScale = 0.95f,
                            animationSpec = tween(POKEDEX_ANIM_MS, easing = PokedexSettlingCurve)
                        )).togetherWith(
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Start,
                                animationSpec = tween(POKEDEX_ANIM_MS, easing = PokedexSettlingCurve)
                            ) + fadeOut(tween(POKEDEX_ANIM_MS / 2))
                        ).using(SizeTransform(clip = false))
                    }
                    backward -> {
                        (slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(POKEDEX_ANIM_MS, easing = PokedexSettlingCurve)
                        ) + fadeIn(tween(POKEDEX_ANIM_MS))).togetherWith(
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.End,
                                animationSpec = tween(POKEDEX_ANIM_MS, easing = PokedexSettlingCurve)
                            ) + fadeOut(tween(POKEDEX_ANIM_MS / 2))
                        ).using(SizeTransform(clip = false))
                    }
                    else -> fadeIn(tween(400)) togetherWith fadeOut(tween(400))
                }
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
                entry<PokedexRoute.Items> { PlaceholderScreen("THE BAG (ITEMS)") }
                entry<PokedexRoute.Moves> { PlaceholderScreen("MOVE-DEX") }
                entry<PokedexRoute.Strategy> { PlaceholderScreen("STRATEGY (TYPES)") }
                entry<PokedexRoute.Options> { OptionsScreen(optionsViewModel = optionsViewModel) }
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
