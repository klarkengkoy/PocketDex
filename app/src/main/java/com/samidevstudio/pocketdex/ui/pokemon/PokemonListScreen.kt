package com.samidevstudio.pocketdex.ui.pokemon

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samidevstudio.pocketdex.ui.pokemon.components.MorphingSearchBar
import com.samidevstudio.pocketdex.ui.pokemon.components.PokemonFilterMenu
import com.samidevstudio.pocketdex.ui.pokemon.components.PokemonGrid
import com.samidevstudio.pocketdex.ui.pokemon.components.PokemonListHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonScreen(
    viewModel: PokemonViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    navigationBottomPadding: Dp,
    onPokemonClick: (PokemonUiModel) -> Unit,
) {
    val stateValue by viewModel.listUiState.collectAsStateWithLifecycle()
    val state = stateValue
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeTypeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()
    
    var isSearchActive by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var showFilterSheet by remember { mutableStateOf(false) }
    
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val searchFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isFocused) {
        isSearchActive = isFocused
    }

    val activePokemonId by viewModel.activePokemonId.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
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
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PokemonListHeader(modifier = Modifier.weight(1f))
                                Spacer(modifier = Modifier.width(64.dp))
                                val filterColor = if (activeTypeFilter.isEmpty()) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                                IconButton(
                                    onClick = { showFilterSheet = true },
                                    modifier = Modifier
                                        .align(Alignment.Bottom)
                                        .size(56.dp)
                                        .border(2.dp, filterColor, RectangleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = if (activeTypeFilter.isEmpty()) {
                                            "Open Type Filter"
                                        } else {
                                            "Open Type Filter, ${activeTypeFilter.size} active"
                                        },
                                        tint = filterColor
                                    )
                                }
                            }
                            MorphingSearchBar(
                                isExpanded = isSearchActive,
                                query = searchQuery,
                                onQueryChange = viewModel::updateSearchQuery,
                                onToggle = { isSearchActive = true },
                                onClear = { viewModel.updateSearchQuery("") },
                                onClose = {
                                    isSearchActive = false
                                    focusManager.clearFocus()
                                },
                                interactionSource = interactionSource,
                                focusRequester = searchFocusRequester,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = (-64).dp, y = 4.dp)
                            )
                        }
                        if (searchQuery.isNotEmpty() || activeTypeFilter.isNotEmpty()) {
                            val activeCriteria = buildList {
                                if (searchQuery.isNotEmpty()) add("SEARCH: ${searchQuery.uppercase()}")
                                if (activeTypeFilter.isNotEmpty()) {
                                    add(activeTypeFilter.sorted().joinToString(" + ") { it.uppercase() })
                                }
                            }.joinToString(" · ")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${state.pokemonList.size} RESULTS",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = activeCriteria,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                                TextButton(onClick = viewModel::clearFilters) { Text("CLEAR ALL") }
                            }
                        }
                        state.errorMessage?.let { message ->
                            Text(message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
                            TextButton(onClick = viewModel::retryLoad) { Text("RETRY") }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        PokemonGrid(
                            pokemonList = state.pokemonList,
                            gridState = gridState,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                            onLoadMore = { viewModel.loadMore() },
                            onSyncPokemon = { id -> viewModel.refreshPokemon(id) },
                            onPokemonClick = { pokemon ->
                                viewModel.activePokemonId.value = pokemon.id
                                onPokemonClick(pokemon)
                            },
                            topPadding = 0.dp,
                            bottomPadding = innerPadding.calculateBottomPadding(),
                            navigationBottomPadding = navigationBottomPadding,
                            clickedPokemonId = activePokemonId
                        )
                    }
                }
                is PokemonListUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "ERROR: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp),
                            fontFamily = FontFamily.Monospace
                        )
                        TextButton(onClick = viewModel::retryLoad) { Text("RETRY") }
                    }
                }
            }


        }

        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                scrimColor = Color.Black.copy(alpha = 0.32f)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "FILTER BY TYPE",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    PokemonFilterMenu(
                        activeTypeFilter = activeTypeFilter,
                        onTypeToggle = { type -> viewModel.toggleTypeFilter(type) },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}
