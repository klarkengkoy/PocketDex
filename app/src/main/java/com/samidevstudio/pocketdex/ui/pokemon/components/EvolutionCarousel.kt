package com.samidevstudio.pocketdex.ui.pokemon.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil.compose.AsyncImage
import com.samidevstudio.pocketdex.ui.navigation.pokemonSpriteTransform
import com.samidevstudio.pocketdex.ui.pokemon.EvolutionNode
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.absoluteValue

@Composable
fun EvolutionCarousel(
    evolutions: List<EvolutionNode>,
    pokemonId: String,
    currentDisplayId: String,
    screenWidth: Dp,
    heroWidth: Dp,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    isBackingOut: Boolean,
    onBackingOutChange: (Boolean) -> Unit,
    onPokemonChange: (String) -> Unit,
    onBack: () -> Unit
) {
    // We use a combination of evolutions.size and the chain ID (if available) 
    // to determine when to reset the pager. Just using 'evolutions' instance 
    // can cause unnecessary resets if the list is re-emitted with same content.
    val chainKey = remember(evolutions) {
        evolutions.joinToString(",") { it.id }
    }

    key(chainKey) {
        // Use currentDisplayId as the anchor for the initial page, NOT the entry pokemonId.
        // This ensures that if the carousel resets (e.g. pop-in), it stays on the 
        // Pokémon the user is currently looking at.
        val initialPage = remember(evolutions, currentDisplayId) {
            val index = evolutions.indexOfFirst { it.id == currentDisplayId }
            if (index != -1) index else {
                // Fallback to entry ID if current one not found
                evolutions.indexOfFirst { it.id == pokemonId }.coerceAtLeast(0)
            }
        }
        
        val pagerState = rememberPagerState(
            initialPage = initialPage
        ) { evolutions.size }

        // Handling back button to fly the carousel back to the starting Pokémon before exiting.
        BackHandler(enabled = !isBackingOut) {
            onBackingOutChange(true)
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
                // CRITICAL FIX: "Compatibility Check"
                // Only trigger a new Pokémon load if:
                // 1. We are not currently exiting the screen.
                // 2. The Pokémon we are parked on is actually different from the current display.
                // 3. MOST IMPORTANT: The current Pokémon we are *supposed* to be looking at 
                //    actually belongs to this evolution chain. This prevents a "Feedback Loop"
                //    where stale carousel data overwrites a fresh detail request.
                val currentPokemonIsInThisChain = evolutions.any { it.id == currentDisplayId }

                if (page < evolutions.size && !isBackingOut && currentPokemonIsInThisChain) {
                    val target = evolutions[page]
                    if (target.id != currentDisplayId) {
                        onPokemonChange(target.id)
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
