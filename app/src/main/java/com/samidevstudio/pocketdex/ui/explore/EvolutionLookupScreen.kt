package com.samidevstudio.pocketdex.ui.explore

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.samidevstudio.pocketdex.ui.components.PocketDexHeader
import com.samidevstudio.pocketdex.ui.pokemon.PokemonListUiState
import com.samidevstudio.pocketdex.ui.pokemon.PokemonViewModel
import com.samidevstudio.pocketdex.ui.theme.retroBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvolutionLookupScreen(
    pokemonViewModel: PokemonViewModel,
    onPokemonSelect: (pokemonId: String, pokemonName: String) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val state by pokemonViewModel.listUiState.collectAsStateWithLifecycle()
    val searchQuery by pokemonViewModel.searchQuery.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
            PocketDexHeader(
                text = "EVOLUTIONS",
                modifier = Modifier.weight(1f),
                includeStatusBarPadding = false
            )
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = pokemonViewModel::updateSearchQuery,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search Pokémon family...", fontFamily = FontFamily.Monospace) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        when (val current = state) {
            is PokemonListUiState.Loading -> {
                Text(
                    text = "LOADING...",
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            is PokemonListUiState.Error -> {
                Column {
                    Text(
                        text = "ERROR: ${current.message}",
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.error
                    )
                    TextButton(onClick = pokemonViewModel::retryLoad) { Text("RETRY") }
                }
            }
            is PokemonListUiState.Success -> {
                current.errorMessage?.let { message ->
                    Text(message, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = pokemonViewModel::retryLoad) { Text("RETRY") }
                }
                if (current.pokemonList.isEmpty()) {
                    Text(
                        text = "No Pokémon found.",
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 180.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(current.pokemonList, key = { _, pokemon -> pokemon.id }) { index, pokemon ->
                            if (index >= current.pokemonList.size - 10) {
                                pokemonViewModel.loadMore()
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .retroBorder()
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                                    .clickable { onPokemonSelect(pokemon.id, pokemon.name) }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    AsyncImage(
                                        model = pokemon.imageUrl,
                                        contentDescription = pokemon.name,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Column {
                                        Text(
                                            text = pokemon.name.uppercase(),
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Tap to view evolution family",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
