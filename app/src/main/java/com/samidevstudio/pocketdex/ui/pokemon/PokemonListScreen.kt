package com.samidevstudio.pocketdex.ui.pokemon

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val searchQuery by viewModel.searchQuery.collectAsState()
    val activeTypeFilter by viewModel.typeFilter.collectAsState()
    val gridState = rememberLazyGridState()
    var isSearchActive by remember { mutableStateOf(false) }
    var isFilterMenuVisible by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val shouldShowSearchField by remember(searchQuery, isSearchActive) {
        derivedStateOf { isSearchActive || searchQuery.isNotEmpty() }
    }

    LaunchedEffect(isFocused, searchQuery.isNotEmpty()) {
        isSearchActive = isFocused || searchQuery.isNotEmpty()
    }

    LaunchedEffect(activeTypeFilter) {
        if (isFilterMenuVisible) {
            gridState.scrollToItem(0)
        }
    }

    // PERFORMANCE: Track which Pokémon is active for shared elements.
    // Observed from ViewModel to sync between List and Detail screens.
    val activePokemonId by viewModel.activePokemonId.collectAsState()

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
                            viewModel.activePokemonId.value = pokemon.id
                            onPokemonClick(pokemon)
                        },
                        topPadding = innerPadding.calculateTopPadding() + 54.dp,
                        clickedPokemonId = activePokemonId
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

            if (isFilterMenuVisible) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    shape = MaterialTheme.shapes.small,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = innerPadding.calculateTopPadding() + 4.dp,
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 8.dp
                        )
                        .retroBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                isFilterMenuVisible = false
                                viewModel.clearTypeFilters()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear filters",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PokemonViewModel.TYPE_FILTER_OPTIONS.forEach { option ->
                                val normalizedOption = option.lowercase()
                                val isSelected = activeTypeFilter.contains(normalizedOption)
                                val colors = PokemonTypeColors.map[normalizedOption] ?: (Color.Gray to Color.Gray)
                                val badgeBrush = Brush.verticalGradient(listOf(colors.first, colors.second))

                                val chipModifier = if (isSelected) {
                                    Modifier
                                        .background(badgeBrush)
                                        .border(1.dp, MaterialTheme.colorScheme.onSurface, MaterialTheme.shapes.small)
                                } else {
                                    Modifier
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), MaterialTheme.shapes.small)
                                }

                                Surface(
                                    color = Color.Transparent,
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier
                                        .clip(MaterialTheme.shapes.small)
                                        .clickable {
                                            viewModel.toggleTypeFilter(normalizedOption)
                                        }
                                        .then(chipModifier)
                                ) {
                                    Text(
                                        text = option.replaceFirstChar { it.titlecase() },
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (shouldShowSearchField) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { value ->
                        viewModel.updateSearchQuery(value)
                        isSearchActive = value.isNotEmpty() || isFocused
                    },
                    placeholder = {
                        Text(
                            text = " Search Pokemon Name or Pokemon ID",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = innerPadding.calculateTopPadding() + 4.dp,
                            bottom = 8.dp
                        )
                        .heightIn(min = 52.dp)
                        .retroBorder(),
                    interactionSource = interactionSource,
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                viewModel.updateSearchQuery("")
                                isSearchActive = false
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = innerPadding.calculateTopPadding() + 4.dp,
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 8.dp
                        )
                        .graphicsLayer { alpha = titleAlpha },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "PocketDex",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                isSearchActive = true
                                isFilterMenuVisible = false
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .retroBorder()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Open search",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = {
                                isFilterMenuVisible = !isFilterMenuVisible
                                if (isFilterMenuVisible) isSearchActive = false
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .retroBorder()
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter Pokémon",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
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
            top = topPadding + 16.dp,
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
                                .fillMaxSize() // Use available weight-based space for maximum "Heroic" scale
                                .padding(vertical = 8.dp) // Subtle breathing room for symmetry
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
