package com.samidevstudio.pocketdex.ui.team

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.samidevstudio.pocketdex.domain.TeamAnalysis
import com.samidevstudio.pocketdex.ui.components.DetailTypeBadge
import com.samidevstudio.pocketdex.ui.components.PocketDexHeader
import com.samidevstudio.pocketdex.ui.theme.rememberRetroIndication
import com.samidevstudio.pocketdex.ui.theme.retroBorder

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeamScreen(
    viewModel: TeamViewModel,
    onAddPokemon: (replacePosition: Int?) -> Unit,
    onPokemonClick: (pokemonId: String, pokemonName: String) -> Unit
) {
    val state by viewModel.teamUiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        PocketDexHeader(text = "TEAM", modifier = Modifier.padding(horizontal = 0.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 0.dp, bottom = 180.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(count = MAX_TEAM_SIZE) { position ->
                val member = state.members.firstOrNull { it.position == position }
                TeamSlotCard(
                    member = member,
                    onAdd = { onAddPokemon(null) },
                    onReplace = { onAddPokemon(position) },
                    onRemove = { viewModel.remove(position) },
                    onClick = { member?.let { onPokemonClick(it.pokemonId, it.pokemonName) } }
                )
            }

            state.analysis?.let { analysis ->
                item { TeamAnalysisSection(analysis) }
            }
        }
    }
}

@Composable
private fun TeamSlotCard(
    member: TeamMemberModel?,
    onAdd: () -> Unit,
    onReplace: () -> Unit,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .retroBorder()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            .semantics(mergeDescendants = true) { }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRetroIndication()
            ) { if (member != null) onClick() else onAdd() }
            .padding(12.dp)
    ) {
        if (member == null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = "Add Pokémon", tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = "ADD POKÉMON",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = member.imageUrl,
                        contentDescription = member.pokemonName,
                        modifier = Modifier.size(56.dp)
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = member.pokemonName.uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            member.types.forEach { type ->
                                Text(
                                    text = type.uppercase(),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
                Row {
                    IconButton(onClick = onReplace) {
                        Icon(Icons.Default.Add, contentDescription = "Replace")
                    }
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Close, contentDescription = "Remove")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TeamAnalysisSection(analysis: TeamAnalysis) {
    Column(
        modifier = Modifier.padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "TEAM ANALYSIS",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (analysis.typeComposition.isNotEmpty()) {
            AnalysisRow("Type composition") {
                analysis.typeComposition.entries.sortedByDescending { it.value }.forEach { (type, count) ->
                    DetailTypeBadge(type = "$type x$count")
                }
            }
        }

        if (analysis.sharedWeaknesses.isNotEmpty()) {
            AnalysisRow("Shared weaknesses") {
                analysis.sharedWeaknesses.forEach { weakness ->
                    DetailTypeBadge(type = "${weakness.type} (${weakness.weakMemberCount})")
                }
            }
        }

        if (analysis.sharedResistances.isNotEmpty()) {
            AnalysisRow("Team-wide resistances") {
                analysis.sharedResistances.forEach { type -> DetailTypeBadge(type = type) }
            }
        }

        if (analysis.sharedImmunities.isNotEmpty()) {
            AnalysisRow("Team-wide immunities") {
                analysis.sharedImmunities.forEach { type -> DetailTypeBadge(type = type) }
            }
        }

        if (analysis.coverageGaps.isNotEmpty()) {
            AnalysisRow("Coverage gaps (no member hits these)") {
                analysis.coverageGaps.forEach { type -> DetailTypeBadge(type = type) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnalysisRow(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title.uppercase(),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            content()
        }
    }
}
