package com.samidevstudio.pocketdex.ui

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.samidevstudio.pocketdex.ui.theme.PokemonTypeColors
import com.samidevstudio.pocketdex.ui.theme.retroBackground
import com.samidevstudio.pocketdex.ui.theme.retroBorder

@Composable
fun PokemonScreen(
    viewModel: PokemonViewModel,
    onPokemonClick: (PokemonUiModel) -> Unit
) {
    val state = viewModel.listUiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .retroBackground()
            .statusBarsPadding()
    ) {
        // Immersive Retro Title
        Text(
            text = "POKEDEX",
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp,
            color = Color.Black
        )

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                is PokemonListUiState.Loading -> {
                    Text(
                        text = "LOADING...",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                is PokemonListUiState.Success -> {
                    PokemonGrid(
                        pokemonList = state.pokemonList,
                        onLoadMore = { viewModel.loadMore() },
                        onPokemonClick = onPokemonClick
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

@Composable
fun PokemonGrid(
    pokemonList: List<PokemonUiModel>,
    onLoadMore: () -> Unit,
    onPokemonClick: (PokemonUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 140.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(pokemonList) { index, pokemon ->
            if (index == pokemonList.size - 1) {
                onLoadMore()
            }
            PokemonCard(
                pokemon = pokemon,
                onClick = { onPokemonClick(pokemon) }
            )
        }
    }
}

@Composable
fun PokemonCard(
    pokemon: PokemonUiModel,
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
            
            Text(
                text = "#${pokemon.id.padStart(4, '0')}",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = pokemon.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    filterQuality = FilterQuality.None, // Sharp retro pixels
                    contentScale = ContentScale.Fit
                )
                
                Text(
                    text = pokemon.name.uppercase(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (pokemon.types.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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

@Composable
fun TypeBadge(type: String) {
    val color = PokemonTypeColors.map[type.lowercase()] ?: Color.Gray
    Surface(
        color = color,
        shape = RectangleShape,
        modifier = Modifier.retroBorder(width = 1.dp)
    ) {
        Text(
            text = type.uppercase(),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}
