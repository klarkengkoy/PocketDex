package com.samidevstudio.pocketdex.ui.pokemon

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.samidevstudio.pocketdex.ui.navigation.pokemonSpriteTransform
import com.samidevstudio.pocketdex.ui.theme.PokemonTypeColors
import com.samidevstudio.pocketdex.ui.theme.retroBackground
import com.samidevstudio.pocketdex.ui.theme.retroBorder

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PokemonScreen(
    viewModel: PokemonViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onPokemonClick: (PokemonUiModel) -> Unit
) {
    val state = viewModel.listUiState
    val gridState = rememberLazyGridState()

    val color1 = MaterialTheme.colorScheme.surface
    val color2 = if (color1.luminance() > 0.5f) {
        Color.Black.copy(alpha = 0.05f).compositeOver(color1)
    } else {
        Color.White.copy(alpha = 0.05f).compositeOver(color1)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "POKEDEX",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier
            .fillMaxSize()
            .retroBackground(color1 = color1, color2 = color2)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                is PokemonListUiState.Loading -> {
                    Text(
                        text = "LOADING...",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                is PokemonListUiState.Success -> {
                    PokemonGrid(
                        pokemonList = state.pokemonList,
                        gridState = gridState,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        onLoadMore = { viewModel.loadMore() },
                        onPokemonClick = onPokemonClick,
                        topPadding = innerPadding.calculateTopPadding()
                    )
                }
                is PokemonListUiState.Error -> {
                    Text(
                        text = "ERROR: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PokemonGrid(
    pokemonList: List<PokemonUiModel>,
    gridState: LazyGridState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onLoadMore: () -> Unit,
    onPokemonClick: (PokemonUiModel) -> Unit,
    topPadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 140.dp),
        state = gridState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = topPadding + 12.dp,
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
            if (index == pokemonList.size - 1) {
                onLoadMore()
            }
            PokemonCard(
                pokemon = pokemon,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onClick = { onPokemonClick(pokemon) }
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PokemonCard(
    pokemon: PokemonUiModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .retroBorder()
            .clickable { onClick() },
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            with(sharedTransitionScope) {
                Text(
                    text = "#${pokemon.id.padStart(4, '0')}",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(key = "pokemon-id-${pokemon.id}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = pokemonSpriteTransform()
                        )
                        .skipToLookaheadSize(),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                with(sharedTransitionScope) {
                    AsyncImage(
                        model = pokemon.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .sharedElement(
                                sharedContentState = rememberSharedContentState(key = "pokemon-image-${pokemon.id}"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                boundsTransform = pokemonSpriteTransform()
                            ),
                        filterQuality = FilterQuality.None,
                        contentScale = ContentScale.Fit
                    )
                }

                with(sharedTransitionScope) {
                    Text(
                        text = pokemon.name.uppercase(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .sharedElement(
                                sharedContentState = rememberSharedContentState(key = "pokemon-name-${pokemon.id}"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                boundsTransform = pokemonSpriteTransform()
                            )
                            .skipToLookaheadSize()
                    )
                }

                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (pokemon.types.isNotEmpty()) {
                        with(sharedTransitionScope) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.sharedElement(
                                    sharedContentState = rememberSharedContentState(key = "pokemon-types-${pokemon.id}"),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    boundsTransform = pokemonSpriteTransform()
                                )
                            ) {
                                pokemon.types.forEach { type ->
                                    TypeBadge(type = type)
                                }
                            }
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

    val brush = Brush.verticalGradient(
        0.5f to colors.first,
        0.5f to colors.second
    )

    Surface(
        color = Color.Transparent,
        shape = RectangleShape,
        modifier = Modifier
            .width(64.dp)
            .background(brush)
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
