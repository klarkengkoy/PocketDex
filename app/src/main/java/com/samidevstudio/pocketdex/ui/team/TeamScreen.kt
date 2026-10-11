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
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.samidevstudio.pocketdex.domain.TypeRelations
import com.samidevstudio.pocketdex.domain.TypeEffectivenessCalculator
import com.samidevstudio.pocketdex.ui.components.DetailTypeBadge
import com.samidevstudio.pocketdex.ui.components.PocketDexHeader
import com.samidevstudio.pocketdex.ui.pokemon.PokemonDetailModel
import com.samidevstudio.pocketdex.ui.theme.rememberRetroIndication
import com.samidevstudio.pocketdex.ui.theme.retroBorder

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeamScreen(
    viewModel: TeamViewModel,
    onAddPokemon: (replacePosition: Int?) -> Unit,
    onPokemonClick: (pokemonId: String, pokemonName: String) -> Unit,
    onCompareSelect: ((compareSlot: Int) -> Unit)? = null
) {
    val state by viewModel.teamUiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        PocketDexHeader(text = "TEAM LAB", modifier = Modifier.padding(horizontal = 0.dp))

        state.typeSyncError?.let { error ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                TextButton(onClick = viewModel::retryTypeSync) { Text("RETRY") }
            }
        }

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

            if (onCompareSelect != null) {
                item {
                    TeamComparisonSection(
                        slot1State = state.compareSlot1,
                        slot2State = state.compareSlot2,
                        onSelectSlot1 = { onCompareSelect(1) },
                        onSelectSlot2 = { onCompareSelect(2) },
                        onRetrySlot1 = { viewModel.retryCompare(1) },
                        onRetrySlot2 = { viewModel.retryCompare(2) },
                        onClear = viewModel::clearComparison,
                        typeRelations = state.typeRelations
                    )
                }
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
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = "Replace ${member.pokemonName}"
                        )
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
                    DetailTypeBadge(type = type, label = "$type: $count")
                }
            }
        }

        if (analysis.sharedWeaknesses.isNotEmpty()) {
            AnalysisRow("Shared weaknesses") {
                analysis.sharedWeaknesses.forEach { weakness ->
                    DetailTypeBadge(
                        type = weakness.type,
                        label = "${weakness.type}: ${weakness.weakMemberCount}"
                    )
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
            AnalysisRow("Type-based coverage gaps") {
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
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun TeamComparisonSection(
    slot1State: CompareSlotState,
    slot2State: CompareSlotState,
    onSelectSlot1: () -> Unit,
    onSelectSlot2: () -> Unit,
    onRetrySlot1: () -> Unit,
    onRetrySlot2: () -> Unit,
    onClear: () -> Unit,
    typeRelations: Map<String, TypeRelations>
) {
    Column(
        modifier = Modifier.padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "POKÉMON COMPARISON",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (slot1State !is CompareSlotState.Empty || slot2State !is CompareSlotState.Empty) {
                TextButton(onClick = onClear) {
                    Text("CLEAR", fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                CompareSlotCard(
                    slotTitle = "POKÉMON 1",
                    state = slot1State,
                    onSelect = onSelectSlot1,
                    onRetry = onRetrySlot1
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                CompareSlotCard(
                    slotTitle = "POKÉMON 2",
                    state = slot2State,
                    onSelect = onSelectSlot2,
                    onRetry = onRetrySlot2
                )
            }
        }

        if (slot1State is CompareSlotState.Success && slot2State is CompareSlotState.Success) {
            CompareTable(
                pokemon1 = slot1State.pokemon,
                pokemon2 = slot2State.pokemon,
                typeRelations = typeRelations
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompareSlotCard(
    slotTitle: String,
    state: CompareSlotState,
    onSelect: () -> Unit,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .retroBorder()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            .padding(10.dp)
    ) {
        when (state) {
            is CompareSlotState.Empty -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(text = slotTitle, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    TextButton(onClick = onSelect) {
                        Text("SELECT", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
            is CompareSlotState.Loading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(text = slotTitle, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    Text("LOADING...", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            is CompareSlotState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(text = slotTitle, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    Text("ERROR: ${state.message}", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = onRetry) { Text("RETRY", fontFamily = FontFamily.Monospace) }
                }
            }
            is CompareSlotState.Success -> {
                val p = state.pokemon
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(model = p.imageUrl, contentDescription = p.name, modifier = Modifier.size(48.dp))
                    Text(text = p.name.uppercase(), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        p.types.forEach { type -> DetailTypeBadge(type = type) }
                    }
                    TextButton(onClick = onSelect) {
                        Text("CHANGE", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CompareTable(
    pokemon1: PokemonDetailModel,
    pokemon2: PokemonDetailModel,
    typeRelations: Map<String, TypeRelations>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .retroBorder()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "BASE STAT COMPARISON",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        val stats1Map = pokemon1.stats.associate { it.name to it.value }
        val stats2Map = pokemon2.stats.associate { it.name to it.value }
        val statNames = (pokemon1.stats.map { it.name } + pokemon2.stats.map { it.name }).distinct()

        statNames.forEach { statName ->
            val v1 = stats1Map[statName] ?: 0
            val v2 = stats2Map[statName] ?: 0
            StatCompareRow(statName = statName, val1 = v1, val2 = v2)
        }

        val total1 = pokemon1.stats.sumOf { it.value }
        val total2 = pokemon2.stats.sumOf { it.value }
        StatCompareRow(statName = "Total", val1 = total1, val2 = total2, isTotal = true)

        Text(
            text = "TYPE EFFECTIVENESS",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "Type-only comparison; excludes moves, abilities, weather, items, and battle conditions.",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        TypeEffectivenessDirection(
            title = "FIRST POKÉMON'S TYPES ATTACKING SECOND POKÉMON",
            attackingTypes = pokemon1.types,
            defendingTypes = pokemon2.types,
            typeRelations = typeRelations
        )
        TypeEffectivenessDirection(
            title = "SECOND POKÉMON'S TYPES ATTACKING FIRST POKÉMON",
            attackingTypes = pokemon2.types,
            defendingTypes = pokemon1.types,
            typeRelations = typeRelations
        )
    }
}

@Composable
private fun TypeEffectivenessDirection(
    title: String,
    attackingTypes: List<String>,
    defendingTypes: List<String>,
    typeRelations: Map<String, TypeRelations>
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.secondary
        )
        val matchups = TypeEffectivenessCalculator.attackMatchups(
            attackingTypes = attackingTypes,
            defendingTypes = defendingTypes,
            allTypes = typeRelations
        )
        if (matchups.isEmpty()) {
            Text(
                text = "TYPE MATCHUP DATA UNAVAILABLE",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
            matchups.forEach { matchup ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DetailTypeBadge(type = matchup.attackingType)
                    Text(
                        text = matchup.multiplier?.let(TypeEffectivenessCalculator::formatMultiplier)
                            ?: "UNAVAILABLE",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCompareRow(
    statName: String,
    val1: Int,
    val2: Int,
    isTotal: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$val1",
            fontFamily = FontFamily.Monospace,
            fontWeight = if (val1 >= val2) FontWeight.ExtraBold else FontWeight.Normal,
            color = if (val1 > val2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = statName.uppercase(),
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isTotal) FontWeight.ExtraBold else FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1.5f)
        )
        Text(
            text = "$val2",
            fontFamily = FontFamily.Monospace,
            fontWeight = if (val2 >= val1) FontWeight.ExtraBold else FontWeight.Normal,
            color = if (val2 > val1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}
