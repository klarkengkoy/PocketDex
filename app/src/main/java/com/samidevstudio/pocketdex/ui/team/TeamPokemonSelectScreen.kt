package com.samidevstudio.pocketdex.ui.team

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.samidevstudio.pocketdex.ui.pokemon.PokemonListUiState
import com.samidevstudio.pocketdex.ui.pokemon.PokemonUiModel
import com.samidevstudio.pocketdex.ui.pokemon.PokemonViewModel
import com.samidevstudio.pocketdex.ui.theme.retroBorder

/**
 * A lightweight Pokémon picker for the Team Builder. Reuses [PokemonViewModel]'s existing
 * cached list/search (the same shared instance backing the Pokédex) instead of duplicating
 * the Pokémon List screen or its data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamPokemonSelectScreen(
    pokemonViewModel: PokemonViewModel,
    replacePosition: Int?,
    onSelect: (PokemonUiModel) -> Unit,
    onBack: () -> Unit
) {
    val state by pokemonViewModel.listUiState.collectAsStateWithLifecycle()
    val searchQuery by pokemonViewModel.searchQuery.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (replacePosition != null) "REPLACE POKÉMON" else "ADD POKÉMON",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = pokemonViewModel::updateSearchQuery,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search Pokémon...", fontFamily = FontFamily.Monospace) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        when (val current = state) {
            is PokemonListUiState.Loading -> Text(
                "LOADING...",
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
            is PokemonListUiState.Error -> Text(
                "ERROR: ${current.message}",
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.error
            )
            is PokemonListUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 180.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(current.pokemonList, key = { it.id }) { pokemon ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .retroBorder()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                                .clickable {
                                    onSelect(pokemon)
                                    onBack()
                                }
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = pokemon.imageUrl,
                                    contentDescription = pokemon.name,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = pokemon.name.uppercase(),
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
