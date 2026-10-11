package com.samidevstudio.pocketdex.ui.berry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samidevstudio.pocketdex.ui.components.ExploreDetailHeader

@Composable
fun BerryDetailScreen(
    berryId: String,
    berryName: String,
    viewModel: BerryViewModel,
    onBack: () -> Unit
) {
    DisposableEffect(berryId) {
        viewModel.loadBerryDetail(berryId)
        onDispose { viewModel.loadBerryDetail(null) }
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
                title = berryName.replace('-', ' ').uppercase(),
                onBack = onBack
            )

            when (val current = state) {
                is BerryDetailUiState.Loading -> Text(
                    text = "LOADING...",
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
                is BerryDetailUiState.Error -> Column {
                    Text(
                        text = "ERROR: ${current.message}",
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.error
                    )
                    TextButton(onClick = viewModel::retryBerryDetail) { Text("RETRY") }
                }
                is BerryDetailUiState.Success -> {
                    val berry = current.berry

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "BERRY STATS",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        BerryStatRow(label = "Growth Time", value = "${berry.growthTime} hours/stage")
                        BerryStatRow(label = "Max Harvest", value = "${berry.maxHarvest}")
                        BerryStatRow(label = "Size", value = "${berry.size} mm")
                        BerryStatRow(label = "Smoothness", value = "${berry.smoothness}")
                        BerryStatRow(label = "Soil Dryness", value = "${berry.soilDryness}")
                        BerryStatRow(label = "Firmness", value = berry.firmness.replace('-', ' ').uppercase())
                        BerryStatRow(label = "Natural Gift Power", value = "${berry.naturalGiftPower}")
                        BerryStatRow(label = "Natural Gift Type", value = berry.naturalGiftType.uppercase())
                    }

                    if (berry.flavors.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "FLAVORS",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                berry.flavors.forEach { flavor ->
                                    val potencyStart = flavor.lastIndexOf(" (")
                                    val (name, potency) = if (
                                        potencyStart >= 0 && flavor.endsWith(')')
                                    ) {
                                        flavor.substring(0, potencyStart) to
                                            flavor.substring(potencyStart + 2, flavor.lastIndex)
                                    } else {
                                        flavor to ""
                                    }
                                    BerryStatRow(
                                        label = name.replace('-', ' ').uppercase(),
                                        value = potency
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

@Composable
private fun BerryStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
