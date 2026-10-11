package com.samidevstudio.pocketdex.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import com.samidevstudio.pocketdex.ui.components.PokedexScaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.samidevstudio.pocketdex.ui.components.ExploreDetailHeader
import com.samidevstudio.pocketdex.ui.pokemon.EvolutionNode
import com.samidevstudio.pocketdex.ui.pokemon.PokemonDetailUiState
import com.samidevstudio.pocketdex.ui.pokemon.PokemonViewModel
import com.samidevstudio.pocketdex.ui.theme.retroBorder

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EvolutionFamilyScreen(
    pokemonId: String,
    pokemonName: String,
    pokemonViewModel: PokemonViewModel,
    onBack: () -> Unit,
    onPokemonClick: (id: String, name: String) -> Unit
) {
    val detailOwner = remember { Any() }
    DisposableEffect(pokemonId, detailOwner) {
        pokemonViewModel.loadPokemonDetail(pokemonId, detailOwner)
        onDispose { pokemonViewModel.clearPokemonDetail(detailOwner) }
    }

    val state by pokemonViewModel.detailUiState.collectAsStateWithLifecycle()

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
                title = "${pokemonName.uppercase()} FAMILY",
                onBack = onBack,
                titleFontSize = 20.sp
            )

            when (val current = state) {
                is PokemonDetailUiState.Loading -> Text(
                    text = "LOADING...",
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
                is PokemonDetailUiState.Error -> Column {
                    Text(
                        text = "ERROR: ${current.message}",
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.error
                    )
                    TextButton(onClick = pokemonViewModel::retryPokemonDetail) { Text("RETRY") }
                }
                is PokemonDetailUiState.Success -> {
                    val rootNode = current.pokemon.evolutions.singleOrNull()

                    if (rootNode == null || rootNode.evolvesTo.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .retroBorder()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "THIS POKÉMON DOES NOT EVOLVE.",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        // Stages view
                        val stages = rootNode.byStages()
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "STAGES IN FAMILY",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            stages.forEachIndexed { stageIndex, stageNodes ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .retroBorder()
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "STAGE $stageIndex",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(
                                            10.dp,
                                            Alignment.CenterHorizontally
                                        ),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        stageNodes.forEach { node ->
                                            EvolutionCard(
                                                node = node,
                                                isCurrent = node.id == pokemonId,
                                                onClick = { onPokemonClick(node.id, node.name) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Branch Paths view
                        val paths = rootNode.allPaths()
                        if (paths.size > 1 || paths.any { it.size > 2 }) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "BRANCHED EVOLUTION PATHS (${paths.size})",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                paths.forEachIndexed { index, path ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .retroBorder()
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "PATH ${index + 1}",
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(
                                                6.dp,
                                                Alignment.CenterHorizontally
                                            )
                                        ) {
                                            path.forEachIndexed { nodeIndex, node ->
                                                if (nodeIndex > 0) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                EvolutionCard(
                                                    node = node,
                                                    isCurrent = node.id == pokemonId,
                                                    onClick = { onPokemonClick(node.id, node.name) }
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
    }
}

@Composable
private fun EvolutionCard(
    node: EvolutionNode,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .retroBorder()
            .background(
                if (isCurrent) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AsyncImage(
                model = node.imageUrl,
                contentDescription = node.name,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = node.name.uppercase(),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
