package com.samidevstudio.pocketdex.ui.region

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samidevstudio.pocketdex.ui.components.ExploreDetailHeader
import androidx.compose.ui.semantics.Role

import androidx.compose.foundation.clickable

@Composable
fun RegionDetailScreen(
    regionId: String,
    regionName: String,
    viewModel: RegionViewModel,
    onBack: () -> Unit,
    onLocationClick: ((String) -> Unit)? = null
) {
    DisposableEffect(regionId) {
        viewModel.loadRegionDetail(regionId)
        onDispose { viewModel.loadRegionDetail(null) }
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
                title = regionName.replace('-', ' ').uppercase(),
                onBack = onBack
            )

            when (val current = state) {
                is RegionDetailUiState.Loading -> Text(
                    text = "LOADING...",
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
                is RegionDetailUiState.Error -> Column {
                    Text(
                        text = "ERROR: ${current.message}",
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.error
                    )
                    TextButton(onClick = viewModel::retryRegionDetail) { Text("RETRY") }
                }
                is RegionDetailUiState.Success -> {
                    val region = current.region

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "REGION DETAILS",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        RegionStatRow(
                            label = "Main Generation",
                            value = region.mainGeneration.replace('-', ' ').uppercase()
                        )
                    }

                    if (region.pokedexes.isNotEmpty()) {
                        RegionTextSection(title = "POKÉDEXES", values = region.pokedexes)
                    }

                    if (region.versionGroups.isNotEmpty()) {
                        RegionTextSection(title = "GAMES / VERSIONS", values = region.versionGroups)
                    }

                    if (region.locationNames.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "LOCATIONS (${region.locationNames.size})",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            region.locationNames.forEachIndexed { index, location ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            enabled = onLocationClick != null,
                                            role = if (onLocationClick != null) Role.Button else null
                                        ) { onLocationClick?.invoke(location) }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = location.replace('-', ' ').uppercase(),
                                        modifier = Modifier.weight(1f),
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (onLocationClick != null) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "Open ${location.replace('-', ' ')}"
                                        )
                                    }
                                }
                                if (index < region.locationNames.lastIndex) {
                                    androidx.compose.material3.HorizontalDivider()
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
private fun RegionStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
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

@Composable
private fun RegionTextSection(title: String, values: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        values.forEach { value ->
            Text(
                text = value.replace('-', ' ').uppercase(),
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
