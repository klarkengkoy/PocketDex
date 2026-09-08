package com.samidevstudio.pocketdex.ui.pokemon

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.samidevstudio.pocketdex.ui.pokemon.components.PokemonFilterMenu
import com.samidevstudio.pocketdex.ui.pokemon.components.PokemonGrid
import com.samidevstudio.pocketdex.ui.pokemon.components.PokemonSearchBar
import com.samidevstudio.pocketdex.ui.pokemon.components.PokemonSearchHeader
import com.samidevstudio.pocketdex.ui.theme.retroBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonScreen(
    viewModel: PokemonViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onPokemonClick: (PokemonUiModel) -> Unit
) {
    val stateValue by viewModel.listUiState.collectAsStateWithLifecycle()
    val state = stateValue
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeTypeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()
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

    val activePokemonId by viewModel.activePokemonId.collectAsStateWithLifecycle()

    val statusBarsPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val safeTopPadding = if (statusBarsPadding > 0.dp) statusBarsPadding else 24.dp

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        val titleAlpha by remember {
            derivedStateOf {
                if (gridState.firstVisibleItemIndex > 0) 0f
                else {
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
                        topPadding = safeTopPadding + innerPadding.calculateTopPadding() + 54.dp,
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

            val topOffset = safeTopPadding + innerPadding.calculateTopPadding() + 4.dp
            
            if (isFilterMenuVisible) {
                PokemonFilterMenu(
                    activeTypeFilter = activeTypeFilter,
                    onTypeToggle = { viewModel.toggleTypeFilter(it) },
                    modifier = Modifier
                        .padding(top = topOffset, start = 16.dp, end = 16.dp, bottom = 8.dp)
                        .retroBorder()
                )
            } else if (shouldShowSearchField) {
                PokemonSearchBar(
                    query = searchQuery,
                    onQueryChange = { 
                        viewModel.updateSearchQuery(it)
                        isSearchActive = it.isNotEmpty() || isFocused
                    },
                    onClearClick = {
                        viewModel.updateSearchQuery("")
                        isSearchActive = false
                    },
                    interactionSource = interactionSource,
                    modifier = Modifier.padding(top = topOffset, start = 16.dp, end = 16.dp, bottom = 8.dp)
                )
            } else {
                PokemonSearchHeader(
                    onSearchClick = {
                        isSearchActive = true
                        isFilterMenuVisible = false
                    },
                    onFilterClick = {
                        isFilterMenuVisible = !isFilterMenuVisible
                        if (isFilterMenuVisible) isSearchActive = false
                    },
                    titleAlpha = titleAlpha,
                    modifier = Modifier.padding(top = safeTopPadding + innerPadding.calculateTopPadding())
                )
            }
        }
    }
}
