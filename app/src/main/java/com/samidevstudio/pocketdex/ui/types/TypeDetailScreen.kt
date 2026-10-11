package com.samidevstudio.pocketdex.ui.types

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.samidevstudio.pocketdex.ui.components.PokedexScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samidevstudio.pocketdex.ui.components.DetailTypeBadge
import com.samidevstudio.pocketdex.ui.components.ExploreDetailHeader
import com.samidevstudio.pocketdex.ui.components.PokemonResultRow
import com.samidevstudio.pocketdex.ui.pokemon.PokemonUiModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TypeDetailScreen(
    typeName: String,
    viewModel: TypeViewModel,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onTypeClick: (String) -> Unit,
    onPokemonClick: (PokemonUiModel) -> Unit
) {
    DisposableEffect(typeName) {
        viewModel.loadTypeDetail(typeName)
        onDispose { viewModel.clearTypeDetail(typeName) }
    }

    val state by viewModel.detailUiState.collectAsStateWithLifecycle()

    PokedexScaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = innerPadding.calculateTopPadding() + 24.dp, start = 16.dp, end = 16.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ExploreDetailHeader(
                title = typeName.uppercase(),
                onBack = onBack
            )

            when (val current = state) {
                is TypeDetailUiState.Loading -> Text(
                    "LOADING...",
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
                is TypeDetailUiState.Error -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "ERROR: ${current.message}",
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.error
                    )
                    TextButton(onClick = onRetry) { Text("RETRY") }
                }
                is TypeDetailUiState.Success -> {
                    if (!current.type.name.equals(typeName, ignoreCase = true)) {
                        Text(
                            "LOADING...",
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        val matchups = current.type.matchups
                        val hasMatchups = matchups.superEffectiveAgainst.isNotEmpty() ||
                                matchups.notVeryEffectiveAgainst.isNotEmpty() ||
                                matchups.noEffectAgainst.isNotEmpty() ||
                                matchups.weaknesses.isNotEmpty() ||
                                matchups.resistances.isNotEmpty() ||
                                matchups.immunities.isNotEmpty()

                        MatchupSection("Super-effective against", matchups.superEffectiveAgainst, onTypeClick)
                        MatchupSection("Not very effective against", matchups.notVeryEffectiveAgainst, onTypeClick)
                        MatchupSection("No effect against", matchups.noEffectAgainst, onTypeClick)
                        MatchupSection("Weak to", matchups.weaknesses, onTypeClick)
                        MatchupSection("Resists", matchups.resistances, onTypeClick)
                        MatchupSection("Immune to", matchups.immunities, onTypeClick)

                        if (!hasMatchups) {
                            Text(
                                "NO TYPE MATCHUP DATA AVAILABLE.",
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (hasMatchups && current.type.pokemon.isNotEmpty()) {
                            HorizontalDivider()
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "POKÉMON (${current.type.pokemon.size})",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (current.type.pokemon.isEmpty()) {
                                Text(
                                    "NO POKÉMON DATA AVAILABLE FOR THIS TYPE.",
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    current.type.pokemon.forEach { pokemon ->
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MatchupSection(
    title: String,
    types: List<String>,
    onTypeClick: (String) -> Unit
) {
    if (types.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title.uppercase(),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            types.forEach { type ->
                DetailTypeBadge(type = type, onClick = { onTypeClick(type) })
            }
        }
    }
}
