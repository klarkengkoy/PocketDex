package com.samidevstudio.pocketdex.ui.navigation

import android.os.Parcelable
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.BoundsTransform
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import com.samidevstudio.pocketdex.ui.items.ItemDetailScreen
import com.samidevstudio.pocketdex.ui.items.ItemListModel
import com.samidevstudio.pocketdex.ui.items.ItemListScreen
import com.samidevstudio.pocketdex.ui.items.ItemViewModel
import com.samidevstudio.pocketdex.ui.moves.MoveDetailScreen
import com.samidevstudio.pocketdex.ui.moves.MoveListModel
import com.samidevstudio.pocketdex.ui.moves.MoveListScreen
import com.samidevstudio.pocketdex.ui.moves.MoveViewModel
import com.samidevstudio.pocketdex.ui.pokemon.PokemonDetailScreen
import com.samidevstudio.pocketdex.ui.pokemon.PokemonScreen
import com.samidevstudio.pocketdex.ui.pokemon.PokemonViewModel
import com.samidevstudio.pocketdex.ui.team.TeamPokemonSelectScreen
import com.samidevstudio.pocketdex.ui.team.TeamScreen
import com.samidevstudio.pocketdex.ui.team.TeamViewModel
import com.samidevstudio.pocketdex.ui.types.TypeDetailScreen
import com.samidevstudio.pocketdex.ui.types.TypeListModel
import com.samidevstudio.pocketdex.ui.types.TypeListScreen
import com.samidevstudio.pocketdex.ui.types.TypeViewModel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Pokedex Animation Standards.
 */
const val POKEDEX_ANIM_MS = 600

// Used for screen slides - starts fast, settles smoothly
val PokedexSettlingCurve = CubicBezierEasing(0.18f, 0.82f, 0.23f, 1.0f)

// Used for Pokémon sprites - smooth flight without overshoot
val SpriteFlightCurve = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)

/**
 * Creates a flight path for Pokémon sprites.
 * Automatically chooses an Arc direction based on the starting position.
 */
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
    data class Detail(val pokemonId: String, val pokemonName: String) : PokedexRoute

    @Serializable
    @Parcelize
    data object Moves : PokedexRoute

    @Serializable
    @Parcelize
    data class MoveDetail(val moveId: String, val moveName: String) : PokedexRoute

    @Serializable
    @Parcelize
    data object Types : PokedexRoute

    @Serializable
    @Parcelize
    data class TypeDetail(val typeName: String) : PokedexRoute

    @Serializable
    @Parcelize
    data object Items : PokedexRoute

    @Serializable
    @Parcelize
    data class ItemDetail(val itemId: String, val itemName: String) : PokedexRoute

    @Serializable
    @Parcelize
    data object Team : PokedexRoute

    @Serializable
    @Parcelize
    data class TeamPokemonSelect(val replacePosition: Int? = null) : PokedexRoute
}

@Composable
fun MainNavigation(
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    // FIX: Pass the Factory to ensure the ViewModel is created with its required repository.
    val pokemonViewModel: PokemonViewModel = viewModel(factory = PokemonViewModel.Factory)
    val moveViewModel: MoveViewModel = viewModel(factory = MoveViewModel.Factory)
    val typeViewModel: TypeViewModel = viewModel(factory = TypeViewModel.Factory)
    val itemViewModel: ItemViewModel = viewModel(factory = ItemViewModel.Factory)
    val teamViewModel: TeamViewModel = viewModel(factory = TeamViewModel.Factory)

    SharedTransitionLayout(modifier = modifier) {
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

                entry<PokedexRoute.Moves> {
                    MoveListScreen(
                        viewModel = moveViewModel,
                        onMoveClick = { move: MoveListModel ->
                            backStack.add(PokedexRoute.MoveDetail(move.id, move.name))
                        }
                    )
                }
                entry<PokedexRoute.MoveDetail> { route ->
                    MoveDetailScreen(
                        moveId = route.moveId,
                        moveName = route.moveName,
                        viewModel = moveViewModel,
                        onBack = onBack
                    )
                }

                entry<PokedexRoute.Types> {
                    TypeListScreen(
                        viewModel = typeViewModel,
                        onTypeClick = { type: TypeListModel ->
                            backStack.add(PokedexRoute.TypeDetail(type.name))
                        }
                    )
                }
                entry<PokedexRoute.TypeDetail> { route ->
                    TypeDetailScreen(
                        typeName = route.typeName,
                        viewModel = typeViewModel,
                        onBack = onBack
                    )
                }

                entry<PokedexRoute.Items> {
                    ItemListScreen(
                        viewModel = itemViewModel,
                        onItemClick = { item: ItemListModel ->
                            backStack.add(PokedexRoute.ItemDetail(item.id, item.name))
                        }
                    )
                }
                entry<PokedexRoute.ItemDetail> { route ->
                    ItemDetailScreen(
                        itemId = route.itemId,
                        itemName = route.itemName,
                        viewModel = itemViewModel,
                        onBack = onBack
                    )
                }

                entry<PokedexRoute.Team> {
                    TeamScreen(
                        viewModel = teamViewModel,
                        onAddPokemon = { replacePosition ->
                            backStack.add(PokedexRoute.TeamPokemonSelect(replacePosition))
                        },
                        onPokemonClick = { pokemonId, pokemonName ->
                            backStack.add(PokedexRoute.Detail(pokemonId, pokemonName))
                        }
                    )
                }
                entry<PokedexRoute.TeamPokemonSelect> { route ->
                    TeamPokemonSelectScreen(
                        pokemonViewModel = pokemonViewModel,
                        replacePosition = route.replacePosition,
                        onSelect = { pokemon ->
                            teamViewModel.addOrReplace(
                                pokemonId = pokemon.id,
                                pokemonName = pokemon.name,
                                imageUrl = pokemon.imageUrl,
                                types = pokemon.types,
                                replacePosition = route.replacePosition
                            )
                        },
                        onBack = onBack
                    )
                }
            }
        )
    }
}
