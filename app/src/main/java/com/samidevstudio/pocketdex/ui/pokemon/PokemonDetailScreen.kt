package com.samidevstudio.pocketdex.ui.pokemon

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.samidevstudio.pocketdex.ui.components.DetailTypeBadge
import com.samidevstudio.pocketdex.ui.navigation.POKEDEX_ANIM_MS
import com.samidevstudio.pocketdex.ui.navigation.PokedexSettlingCurve
import com.samidevstudio.pocketdex.ui.navigation.pokemonSpriteTransform
import com.samidevstudio.pocketdex.ui.pokemon.components.EvolutionCarousel
import com.samidevstudio.pocketdex.ui.pokemon.components.PokemonDescriptionCard
import com.samidevstudio.pocketdex.ui.pokemon.components.PokemonStatsCard
import com.samidevstudio.pocketdex.ui.theme.retroBackground

@Composable
fun AnimatedLoadingText() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val dotCount by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "dots",
    )

    val dots = ".".repeat(dotCount.toInt())
    
    Text(
        text = "LOADING$dots",
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Start,
        modifier = Modifier.width(120.dp) // Fixed width to prevent shifting
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailScreen(
    pokemonId: String,
    pokemonName: String,
    viewModel: PokemonViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBack: () -> Unit
) {
    // Isolated state key ensures a clean UI reset when navigating to a different Pokémon from the list.
    key(pokemonId) {
        DisposableEffect(pokemonId) {
            viewModel.loadPokemonDetail(pokemonId)
            onDispose {
                // Clear state when leaving this Pokémon's detail view to prevent "ghost" data
                // from leaking into the next Pokémon's screen.
                viewModel.loadPokemonDetail(null)
            }
        }

        val stateValue by viewModel.detailUiState.collectAsState()
        val state = stateValue
        val scrollState = rememberScrollState()
        
        var isBackingOut by remember { mutableStateOf(false) }
        
        // Persist the last successful family tree to prevent the carousel from 
        // flickering/resetting when swiping between members of the same family.
        var lastValidEvolutions by remember { mutableStateOf<List<EvolutionNode>>(emptyList()) }
        
        LaunchedEffect(state) {
            if (state is PokemonDetailUiState.Success) {
                lastValidEvolutions = state.pokemon.evolutions
            }
        }

        val color1 = MaterialTheme.colorScheme.surface
        val color2 = if (color1.luminance() > 0.5f) {
            Color.Black.copy(alpha = 0.05f).compositeOver(color1)
        } else {
            Color.White.copy(alpha = 0.05f).compositeOver(color1)
        }

        val selectedPokemonName = remember(state) {
            if (state is PokemonDetailUiState.Success) {
                state.pokemon.name.uppercase()
            } else {
                pokemonName.uppercase()
            }
        }

        val currentDisplayId = remember(state, pokemonId) {
            if (state is PokemonDetailUiState.Success) {
                state.pokemon.id
            } else {
                pokemonId
            }
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        with(sharedTransitionScope) {
                            Text(
                                text = pokemonName.uppercase(),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .sharedElement(
                                        sharedContentState = rememberSharedContentState(key = "pokemon-name-$pokemonId"),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                        boundsTransform = pokemonSpriteTransform()
                                    )
                                    .skipToLookaheadSize()
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { if (!isBackingOut) isBackingOut = true }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent,
            modifier = Modifier
                .fillMaxSize()
                .retroBackground(color1 = color1, color2 = color2)
        ) { innerPadding ->
            val density = LocalDensity.current
            val windowInfo = LocalWindowInfo.current
            val screenWidth = with(density) { windowInfo.containerSize.width.toDp() }
            val heroWidth = screenWidth * 0.65f 

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(
                        top = innerPadding.calculateTopPadding() + 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 200.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (state) {
                    is PokemonDetailUiState.Loading if lastValidEvolutions.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedLoadingText()
                        }
                    }

                    is PokemonDetailUiState.Error -> {
                        Box(
                            modifier = Modifier
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "ERROR: ${state.message}", color = MaterialTheme.colorScheme.error)
                        }
                    }

                    else -> {
                        // Success OR (Loading + we have cached family tree)
                        val evolutions = if (state is PokemonDetailUiState.Success) {
                            state.pokemon.evolutions
                        } else {
                            lastValidEvolutions
                        }

                        // "Pop-in" Logic:
                        // If evolution chain hasn't loaded yet, show the current Pokémon as the only item.
                        val displayList = evolutions.ifEmpty {
                            listOf(
                                EvolutionNode(
                                    id = currentDisplayId,
                                    name = pokemonName,
                                    imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$currentDisplayId.png"
                                )
                            )
                        }

                        EvolutionCarousel(
                            evolutions = displayList,
                            pokemonId = pokemonId,
                            currentDisplayId = currentDisplayId,
                            screenWidth = screenWidth,
                            heroWidth = heroWidth,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                            isBackingOut = isBackingOut,
                            onBackingOutChange = { isBackingOut = it },
                            onPokemonChange = { viewModel.loadPokemonDetail(it) },
                            onBack = onBack
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (state is PokemonDetailUiState.Success) {
                    val pokemon = state.pokemon
                    
                    with(sharedTransitionScope) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .sharedElement(
                                    sharedContentState = rememberSharedContentState(key = "pokemon-types-$pokemonId"),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    boundsTransform = pokemonSpriteTransform()
                                )
                        ) {
                            pokemon.types.forEach { type ->
                                DetailTypeBadge(type = type)
                            }
                        }
                    }
                    
                    with(animatedVisibilityScope) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.animateEnterExit(
                                enter = fadeIn(tween(POKEDEX_ANIM_MS, delayMillis = 150)) + 
                                        slideInVertically(tween(POKEDEX_ANIM_MS, delayMillis = 150, easing = PokedexSettlingCurve)) { it / 4 },
                                exit = fadeOut(tween(300)) + 
                                        slideOutVertically(tween(300)) { it / 4 }
                            )
                        ) {
                            PokemonStatsCard(
                                pokemon = pokemon,
                                displayName = selectedPokemonName,
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope
                            )

                            if (pokemon.flavorText.isNotEmpty()) {
                                PokemonDescriptionCard(description = pokemon.flavorText)
                            }
                        }
                    }
                }
            }
        }
    }
}
