package com.samidevstudio.pocketdex.ui.pokemon.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.samidevstudio.pocketdex.ui.components.PocketDexHeader
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.samidevstudio.pocketdex.ui.navigation.pokemonSpriteTransform
import com.samidevstudio.pocketdex.ui.pokemon.PokemonUiModel
import com.samidevstudio.pocketdex.ui.pokemon.PokemonViewModel
import com.samidevstudio.pocketdex.ui.theme.PokemonTypeColors
import com.samidevstudio.pocketdex.ui.theme.rememberRetroIndication
import com.samidevstudio.pocketdex.ui.theme.retroBorder

@Composable
fun PokemonListHeader(
    modifier: Modifier = Modifier
) {
    PocketDexHeader(
        text = "POKEMON",
        modifier = modifier
    )
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PokemonFilterMenu(
    activeTypeFilter: Set<String>,
    onTypeToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.Transparent,
        modifier = modifier.fillMaxWidth()
    ) {
        FlowRow(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PokemonViewModel.TYPE_FILTER_OPTIONS.forEach { option ->
                val normalizedOption = option.lowercase()
                val isSelected = activeTypeFilter.contains(normalizedOption)
                val colors = PokemonTypeColors.map[normalizedOption] ?: (Color.Gray to Color.Gray)
                val badgeBrush = Brush.verticalGradient(listOf(colors.first, colors.second))

                val chipModifier = if (isSelected) {
                    Modifier
                        .background(badgeBrush)
                        .border(2.dp, Color.Black, MaterialTheme.shapes.small)
                } else {
                    Modifier
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), MaterialTheme.shapes.small)
                }

                Surface(
                    color = Color.Transparent,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(MaterialTheme.shapes.small)
                        .clickable {
                            onTypeToggle(normalizedOption)
                        }
                        .then(chipModifier)
                ) {
                    Text(
                        text = option.replaceFirstChar { it.uppercase() },
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MorphingSearchBar(
    isExpanded: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onToggle: () -> Unit,
    onClear: () -> Unit,
    focusRequester: FocusRequester,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier
) {
    val widthPercent by animateDpAsState(
        targetValue = if (isExpanded) 240.dp else 56.dp,
        label = "WidthAnimation"
    )

    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = CircleShape,
        modifier = modifier
            .width(widthPercent)
            .height(64.dp)
            .animateContentSize()
            .then(if (isExpanded) Modifier.retroBorder() else Modifier)
            .clickable(
                enabled = !isExpanded,
                onClick = onToggle
            ),
        tonalElevation = 4.dp,
        shadowElevation = 6.dp
    ) {
        if (!isExpanded) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Dex",
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = {
                        Text(
                            text = "Search Dex...",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.onPrimary,
                        focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedTextColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    interactionSource = interactionSource,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )

                IconButton(
                    onClick = onClear,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close search",
                        modifier = Modifier.size(20.dp)
                    )
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
    onSyncPokemon: (String) -> Unit,
    onPokemonClick: (PokemonUiModel) -> Unit,
    topPadding: Dp,
    bottomPadding: Dp,
    navigationBottomPadding: Dp,
    clickedPokemonId: String?,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        state = gridState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = topPadding,
            start = 12.dp,
            end = 12.dp,
            bottom = bottomPadding + navigationBottomPadding + 100.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = pokemonList,
            key = { _, pokemon -> pokemon.id }
        ) { index, pokemon ->
            if (index >= pokemonList.size - 10) {
                onLoadMore()
            }
            
            if (pokemon.types.isEmpty()) {
                LaunchedEffect(pokemon.id) {
                    onSyncPokemon(pokemon.id)
                }
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
    val retroIndication = rememberRetroIndication()
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .graphicsLayer {
                clip = true
            }
            .retroBorder()
            .semantics(mergeDescendants = true) { }
            .clickable(
                interactionSource = interactionSource,
                indication = retroIndication,
                onClick = onClick
            ),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                with(sharedTransitionScope) {
                    Text(
                        text = "#${pokemon.id.padStart(4, '0')}",
                        modifier = Modifier
                            .align(Alignment.End)
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

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    with(sharedTransitionScope) {
                        AsyncImage(
                            model = pokemon.imageUrl,
                            contentDescription = pokemon.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 8.dp)
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
