package com.samidevstudio.pocketdex.ui.pokemon

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.samidevstudio.pocketdex.ui.navigation.POKEDEX_ANIM_MS
import com.samidevstudio.pocketdex.ui.navigation.PokedexSettlingCurve
import com.samidevstudio.pocketdex.ui.navigation.pokemonSpriteTransform
import com.samidevstudio.pocketdex.ui.theme.PokemonTypeColors
import com.samidevstudio.pocketdex.ui.theme.retroBackground
import com.samidevstudio.pocketdex.ui.theme.retroBorder

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun PokemonDetailScreen(
    pokemonId: String,
    pokemonName: String,
    viewModel: PokemonViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBack: () -> Unit
) {
    LaunchedEffect(pokemonId) {
        viewModel.loadPokemonDetail(pokemonId)
    }

    val state = viewModel.detailUiState

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
                    IconButton(onClick = onBack) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$pokemonId.png"
            
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .retroBorder()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                with(sharedTransitionScope) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = pokemonName,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .sharedElement(
                                sharedContentState = rememberSharedContentState(key = "pokemon-image-$pokemonId"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                boundsTransform = pokemonSpriteTransform()
                            ),
                        filterQuality = FilterQuality.None,
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (state) {
                is PokemonDetailUiState.Loading -> {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = "LOADING...",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                is PokemonDetailUiState.Success -> {
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
                            state.pokemon.types.forEach { type ->
                                DetailTypeBadge(type = type)
                            }
                        }
                    }
                    
                    with(animatedVisibilityScope) {
                        Box(
                            modifier = Modifier.animateEnterExit(
                                enter = fadeIn(tween(POKEDEX_ANIM_MS, delayMillis = 150)) + 
                                        slideInVertically(tween(POKEDEX_ANIM_MS, delayMillis = 150, easing = PokedexSettlingCurve)) { it / 4 },
                                exit = fadeOut(tween(300)) + 
                                        slideOutVertically(tween(300)) { it / 4 }
                            )
                        ) {
                            PokemonStatsCard(
                                pokemon = state.pokemon,
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        }
                    }
                }
                is PokemonDetailUiState.Error -> {
                    Text(
                        text = "ERROR: ${state.message}", 
                        color = MaterialTheme.colorScheme.error, 
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PokemonStatsCard(
    pokemon: PokemonDetailModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .retroBorder()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            with(sharedTransitionScope) {
                Text(
                    text = "ID: #${pokemon.id.padStart(4, '0')}",
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(key = "pokemon-id-${pokemon.id}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = pokemonSpriteTransform()
                        )
                        .skipToLookaheadSize(),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "HEIGHT: ${pokemon.height / 10.0}m", 
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "WEIGHT: ${pokemon.weight / 10.0}kg", 
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "BASE STATS:", 
                fontWeight = FontWeight.ExtraBold, 
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            pokemon.stats.forEach { stat ->
                RetroStatBar(name = stat.name, value = stat.value)
            }
        }
    }
}

@Composable
fun DetailTypeBadge(type: String) {
    val colors = PokemonTypeColors.map[type.lowercase()] ?: (Color.Gray to Color.Gray)
    
    val brush = Brush.verticalGradient(
        0.5f to colors.first,
        0.5f to colors.second
    )

    Surface(
        color = Color.Transparent,
        shape = RectangleShape,
        modifier = Modifier
            .width(90.dp)
            .background(brush)
            .border(1.dp, MaterialTheme.colorScheme.onSurface, RectangleShape)
    ) {
        Text(
            text = type.uppercase(),
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(vertical = 4.dp)
        )
    }
}

@Composable
fun RetroStatBar(name: String, value: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = name.uppercase().take(5),
            modifier = Modifier.width(50.dp),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .border(1.dp, MaterialTheme.colorScheme.onSurface)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            val progress = (value / 255f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        Text(
            text = value.toString().padStart(3),
            modifier = Modifier.padding(start = 8.dp),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
