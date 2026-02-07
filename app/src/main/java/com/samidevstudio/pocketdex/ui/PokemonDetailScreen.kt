package com.samidevstudio.pocketdex.ui

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.samidevstudio.pocketdex.ui.theme.retroBackground
import com.samidevstudio.pocketdex.ui.theme.retroBorder

/**
 * PokemonDetailScreen displays detailed info for a selected Pokémon.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailScreen(
    pokemonId: String,
    pokemonName: String,
    viewModel: PokemonViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(pokemonId) {
        viewModel.loadPokemonDetail(pokemonId)
    }

    val state = viewModel.detailUiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = pokemonName.uppercase(), 
                        fontFamily = FontFamily.Monospace, 
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.retroBackground()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$pokemonId.png"
            
            // Image Container
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .retroBorder()
                    .background(Color.White.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = pokemonName,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    filterQuality = FilterQuality.None, // Sharp retro pixels
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Content State Handling
            when (state) {
                is PokemonDetailUiState.Loading -> {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = "LOADING...",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
                is PokemonDetailUiState.Success -> {
                    PokemonStatsCard(pokemon = state.pokemon)
                }
                is PokemonDetailUiState.Error -> {
                    Text(
                        text = "ERROR: ${state.message}", 
                        color = Color.Red, 
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PokemonStatsCard(pokemon: PokemonDetailModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .retroBorder()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "ID: #${pokemon.id.padStart(4, '0')}",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "HEIGHT: ${pokemon.height / 10.0}m", fontFamily = FontFamily.Monospace)
                Text(text = "WEIGHT: ${pokemon.weight / 10.0}kg", fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(text = "BASE STATS:", fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
            
            pokemon.stats.forEach { stat ->
                RetroStatBar(name = stat.name, value = stat.value)
            }
        }
    }
}

@Composable
fun RetroStatBar(name: String, value: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = name.uppercase().take(5),
            modifier = Modifier.width(50.dp),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .retroBorder(width = 1.dp)
                .background(Color.LightGray)
        ) {
            val progress = (value / 255f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(Color.DarkGray)
            )
        }
        Text(
            text = value.toString().padStart(3),
            modifier = Modifier.padding(start = 8.dp),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp
        )
    }
}