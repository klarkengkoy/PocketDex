package com.samidevstudio.pocketdex.ui.pokemon

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.samidevstudio.pocketdex.ui.navigation.pokemonSpriteTransform
import com.samidevstudio.pocketdex.ui.theme.PokemonTypeColors
import com.samidevstudio.pocketdex.ui.theme.retroBackground
import com.samidevstudio.pocketdex.ui.theme.retroBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonScreen(
    viewModel: PokemonViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onPokemonClick: (PokemonUiModel) -> Unit
) {
    val stateValue by viewModel.listUiState.collectAsState()
    val state = stateValue
    val gridState = rememberLazyGridState()
    
    // PERFORMANCE: Track which Pokémon was clicked to only apply shared elements to that specific card.
    // This reduces bound tracking from 200+ targets to just 4.
    var clickedPokemonId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        val titleAlpha by remember {
            derivedStateOf {
                if (gridState.firstVisibleItemIndex > 0) 0f
                else {
                    // Fade out completely within 50 pixels of scroll
                    (1f - (gridState.firstVisibleItemScrollOffset / 50f)).coerceIn(0f, 1f)
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (state) {
                is PokemonListUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "LOADING...",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                is PokemonListUiState.Success -> {
                    PokemonGrid(
                        pokemonList = state.pokemonList,
                        gridState = gridState,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        onLoadMore = { viewModel.loadMore() },
                        onPokemonClick = { pokemon ->
                            clickedPokemonId = pokemon.id
                            onPokemonClick(pokemon)
                        },
                        topPadding = innerPadding.calculateTopPadding(),
                        clickedPokemonId = clickedPokemonId
                    )
                }
                is PokemonListUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "ERROR: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Fixed POKEDEX Title that fades out
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = innerPadding.calculateTopPadding() + 8.dp)
                    .graphicsLayer { alpha = titleAlpha },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "POKEDEX",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun PokemonGrid(
    pokemonList: List<PokemonUiModel>,
    gridState: LazyGridState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onLoadMore: () -> Unit,
    onPokemonClick: (PokemonUiModel) -> Unit,
    topPadding: androidx.compose.ui.unit.Dp,
    clickedPokemonId: String?,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 140.dp),
        state = gridState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = topPadding + 64.dp, // Match the height of the fixed header
            start = 12.dp,
            end = 12.dp,
            bottom = 200.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = pokemonList,
            key = { _, pokemon -> pokemon.id }
        ) { index, pokemon ->
            // Trigger pre-fetch when we are 10 items from the bottom
            if (index >= pokemonList.size - 10) {
                onLoadMore()
            }
            PokemonCard(
                pokemon = pokemon,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                isSharedElementEnabled = pokemon.id == clickedPokemonId,
                onClick = { onPokemonClick(pokemon) }
            )
        }
    }
}

@Composable
fun PokemonCard(
    pokemon: PokemonUiModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    isSharedElementEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .graphicsLayer {
                // Layer caching prevents unnecessary redrawing during scrolls
                clip = true
            }
            .retroBorder()
            .clickable { onClick() },
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp), // Symmetric anchor padding
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Top Section: ID
                with(sharedTransitionScope) {
                    Text(
                        text = "#${pokemon.id.padStart(4, '0')}",
                        modifier = Modifier
                            .align(Alignment.End) // Positioned at the top right
                            .then(
                                if (isSharedElementEnabled) {
                                    Modifier.sharedElement(
                                        sharedContentState = rememberSharedContentState(key = "pokemon-id-${pokemon.id}"),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                        boundsTransform = pokemonSpriteTransform()
                                    )
                                } else Modifier
                            )
                            .skipToLookaheadSize(),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }

                // 2. Middle Section: Sprite (Centered in the gap)
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    with(sharedTransitionScope) {
                        AsyncImage(
                            model = pokemon.imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(110.dp) // Increased size for overlap and heroism
                                .then(
                                    if (isSharedElementEnabled) {
                                        Modifier.sharedElement(
                                            sharedContentState = rememberSharedContentState(key = "pokemon-image-${pokemon.id}"),
                                            animatedVisibilityScope = animatedVisibilityScope,
                                            boundsTransform = pokemonSpriteTransform()
                                        )
                                    } else Modifier
                                ),
                            filterQuality = FilterQuality.None,
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // 3. Bottom Section: Name and Types
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val displayName = remember(pokemon.name) {
                        if (pokemon.name.contains("-") && pokemon.name.length > 10) {
                            pokemon.name.replace("-", "- ")
                        } else {
                            pokemon.name
                        }
                    }

                    with(sharedTransitionScope) {
                        Text(
                            text = displayName.uppercase(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .skipToLookaheadSize()
                                .then(
                                    if (isSharedElementEnabled) {
                                        Modifier.sharedElement(
                                            sharedContentState = rememberSharedContentState(key = "pokemon-name-${pokemon.id}"),
                                            animatedVisibilityScope = animatedVisibilityScope,
                                            boundsTransform = pokemonSpriteTransform()
                                        )
                                    } else Modifier
                                )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(20.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (pokemon.types.isNotEmpty()) {
                            with(sharedTransitionScope) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.then(
                                        if (isSharedElementEnabled) {
                                            Modifier.sharedElement(
                                                sharedContentState = rememberSharedContentState(key = "pokemon-types-${pokemon.id}"),
                                                animatedVisibilityScope = animatedVisibilityScope,
                                                boundsTransform = pokemonSpriteTransform()
                                            )
                                        } else Modifier
                                    )
                                ) {
                                    pokemon.types.forEach { type ->
                                        TypeBadge(type = type)
                                    }
                                }
                            }
                        } else {
                            // Maintains footer height symmetry even before types load
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TypeBadge(type: String) {
    val colors = PokemonTypeColors.map[type.lowercase()] ?: (Color.Gray to Color.Gray)

    // PERFORMANCE: Use drawWithCache to avoid recreating the Brush object on every frame
    Surface(
        color = Color.Transparent,
        shape = RectangleShape,
        modifier = Modifier
            .width(64.dp)
            .drawWithCache {
                val brush = Brush.verticalGradient(
                    0.5f to colors.first,
                    0.5f to colors.second
                )
                onDrawBehind {
                    drawRect(brush)
                }
            }
            .retroBorder(width = 1.dp)
    ) {
        Text(
            text = type.uppercase(),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 2.dp)
        )
    }
}
