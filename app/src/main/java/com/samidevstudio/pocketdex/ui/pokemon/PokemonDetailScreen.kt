package com.samidevstudio.pocketdex.ui.pokemon

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import coil.compose.AsyncImage
import com.samidevstudio.pocketdex.ui.navigation.POKEDEX_ANIM_MS
import com.samidevstudio.pocketdex.ui.navigation.PokedexSettlingCurve
import com.samidevstudio.pocketdex.ui.navigation.pokemonSpriteTransform
import com.samidevstudio.pocketdex.ui.theme.PokemonTypeColors
import com.samidevstudio.pocketdex.ui.theme.retroBackground
import com.samidevstudio.pocketdex.ui.theme.retroBorder
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.absoluteValue

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
    // Isolated state key ensures a clean UI reset when navigating to a different Pokémon from the list.
    key(pokemonId) {
        LaunchedEffect(pokemonId) {
            viewModel.loadPokemonDetail(pokemonId, isInitialEntry = true)
        }

        val state = viewModel.detailUiState
        val scrollState = rememberScrollState()
        
        var isBackingOut by remember { mutableStateOf(false) }

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
                    is PokemonDetailUiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .retroBorder()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "LOADING...",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    is PokemonDetailUiState.Success -> {
                        val evolutions = state.pokemon.evolutions
                        
                        if (evolutions.size > 1) {
                            Text(
                                text = "EVOLUTION CHAIN",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp, 
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp, start = 8.dp),
                                textAlign = TextAlign.Start
                            )
                        }

                        if (evolutions.isEmpty()) {
                            PokemonHeroImage(
                                pokemonId = pokemonId,
                                pokemonName = pokemonName,
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        } else {
                            val initialPage = remember(evolutions) {
                                evolutions.indexOfFirst { it.id == pokemonId }.coerceAtLeast(0)
                            }
                            
                            val pagerState = rememberPagerState(
                                initialPage = initialPage
                            ) { evolutions.size }

                            // Handling back button to fly the carousel back to the starting Pokémon before exiting.
                            BackHandler(enabled = !isBackingOut) {
                                isBackingOut = true
                            }

                            LaunchedEffect(isBackingOut) {
                                if (isBackingOut) {
                                    val originIndex = evolutions.indexOfFirst { it.id == pokemonId }
                                    if (originIndex != -1 && pagerState.currentPage != originIndex) {
                                        pagerState.animateScrollToPage(originIndex)
                                    }
                                    onBack() 
                                }
                            }

                            LaunchedEffect(pagerState, currentDisplayId) {
                                snapshotFlow { pagerState.currentPage }.collectLatest { page ->
                                    if (page < evolutions.size && !isBackingOut) {
                                        val target = evolutions[page]
                                        if (target.id != currentDisplayId) {
                                            viewModel.loadPokemonDetail(target.id)
                                        }
                                    }
                                }
                            }

                            HorizontalPager(
                                state = pagerState,
                                contentPadding = PaddingValues(horizontal = (screenWidth - heroWidth) / 2),
                                pageSize = PageSize.Fixed(heroWidth),
                                pageSpacing = 16.dp,
                                beyondViewportPageCount = 1,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(320.dp)
                            ) { page ->
                                val node = evolutions[page]
                                val isHeroData = node.id == currentDisplayId
                                
                                Box(
                                    modifier = Modifier
                                        .graphicsLayer {
                                            val absoluteOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                                            val fraction = (1f - absoluteOffset.absoluteValue.coerceIn(0f, 1f))
                                            
                                            val widthScale = lerp(0.35f, 1f, fraction)
                                            this.scaleX = widthScale
                                            this.scaleY = 1.0f 
                                            this.alpha = lerp(0.4f, 1f, fraction)
                                            
                                            val cornerSize = lerp(100f, 32f, fraction).dp.toPx()
                                            this.shape = RoundedCornerShape(cornerSize)
                                            this.clip = true

                                            val moveAmount = (1f - widthScale) * size.width * 0.65f
                                            this.translationX = if (absoluteOffset > 0) moveAmount else if (absoluteOffset < 0) -moveAmount else 0f
                                        }
                                        .fillMaxSize()
                                        .background(
                                            if (isHeroData)
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            else
                                                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer {
                                                val absoluteOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                                                val fraction = (1f - absoluteOffset.absoluteValue.coerceIn(0f, 1f))
                                                val widthScale = lerp(0.35f, 1f, fraction)
                                                this.scaleX = 1f / widthScale
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        with(sharedTransitionScope) {
                                            AsyncImage(
                                                model = node.imageUrl,
                                                contentDescription = node.name,
                                                modifier = Modifier
                                                    .size(240.dp)
                                                    .padding(16.dp)
                                                    .then(
                                                        if (node.id == pokemonId) {
                                                            Modifier.sharedElement(
                                                                sharedContentState = rememberSharedContentState(key = "pokemon-image-$pokemonId"),
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
                                }
                            }
                        }
                    }
                    is PokemonDetailUiState.Error -> {
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .retroBorder()
                                .background(MaterialTheme.colorScheme.errorContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "ERROR", color = MaterialTheme.colorScheme.onErrorContainer)
                        }
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PokemonHeroImage(
    pokemonId: String,
    pokemonName: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$pokemonId.png"
    Box(
        modifier = Modifier
            .size(240.dp)
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
}

@Composable
fun PokemonDescriptionCard(description: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .retroBorder()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "DESCRIPTION:", 
                fontWeight = FontWeight.ExtraBold, 
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PokemonStatsCard(
    pokemon: PokemonDetailModel,
    displayName: String,
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
            Text(
                text = displayName,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

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
