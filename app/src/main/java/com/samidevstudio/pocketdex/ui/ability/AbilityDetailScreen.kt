package com.samidevstudio.pocketdex.ui.ability

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import com.samidevstudio.pocketdex.ui.components.PokedexScaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.samidevstudio.pocketdex.ui.components.ExploreDetailHeader
import com.samidevstudio.pocketdex.ui.components.PokemonResultRow
import com.samidevstudio.pocketdex.ui.pokemon.PokemonUiModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AbilityDetailScreen(
    abilityId: String,
    abilityName: String,
    viewModel: AbilityViewModel,
    onBack: () -> Unit,
    onPokemonClick: (PokemonUiModel) -> Unit
) {
    DisposableEffect(abilityId) {
        viewModel.loadAbilityDetail(abilityId)
        onDispose { viewModel.loadAbilityDetail(null) }
    }

    val state by viewModel.detailUiState.collectAsStateWithLifecycle()

    PokedexScaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = innerPadding.calculateTopPadding() + 24.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 180.dp
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ExploreDetailHeader(
                title = abilityName.replace('-', ' ').uppercase(),
                onBack = onBack
            )

            when (val current = state) {
                is AbilityDetailUiState.Loading -> Text(
                    text = "LOADING...",
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
                is AbilityDetailUiState.Error -> Column {
                    Text(
                        text = "ERROR: ${current.message}",
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.error
                    )
                    TextButton(onClick = viewModel::retryAbilityDetail) { Text("RETRY") }
                }
                is AbilityDetailUiState.Success -> {
                    val detail = current.ability

                    if (detail.shortEffect.isNotEmpty() || detail.effect.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "EFFECT",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = detail.shortEffect.ifEmpty { detail.effect },
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    if (detail.flavorText.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "DESCRIPTION",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = detail.flavorText,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "POKÉMON WITH THIS ABILITY (${detail.pokemon.size})",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (detail.pokemon.isEmpty()) {
                            Text(
                                text = "NO POKÉMON ARE ASSOCIATED WITH THIS ABILITY.",
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                detail.pokemon.forEach { pokemon ->
                                    PokemonResultRow(
                                        pokemon = pokemon,
                                        onClick = { onPokemonClick(pokemon) }
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
