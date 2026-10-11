package com.samidevstudio.pocketdex.ui.location

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samidevstudio.pocketdex.ui.components.ExploreDetailHeader

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LocationDetailScreen(
    locationId: String,
    locationName: String,
    viewModel: LocationViewModel,
    onBack: () -> Unit
) {
    DisposableEffect(locationId) {
        viewModel.loadLocationDetail(locationId)
        onDispose { viewModel.loadLocationDetail(null) }
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
                title = locationName.replace('-', ' ').uppercase(),
                onBack = onBack
            )

            when (val current = state) {
                is LocationDetailUiState.Loading -> Text(
                    text = "LOADING...",
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
                is LocationDetailUiState.Error -> Column {
                    Text(
                        text = "ERROR: ${current.message}",
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.error
                    )
                    TextButton(onClick = viewModel::retryLocationDetail) { Text("RETRY") }
                }
                is LocationDetailUiState.Success -> {
                    val location = current.location

                    if (location.regionName.isNotBlank() &&
                        !location.regionName.equals("unknown", ignoreCase = true)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "REGION",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            LocationStatRow(
                                label = "Region",
                                value = location.regionName.replace('-', ' ').uppercase()
                            )
                        }
                    }

                    if (location.areas.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "AREAS",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                location.areas.forEach { area ->
                                    Text(
                                        text = area.replace('-', ' ').uppercase(),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    if (location.gameIndices.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "GAME APPEARANCES",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                location.gameIndices.forEach { gameIndex ->
                                    Text(
                                        text = gameIndex.uppercase(),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    if (location.regionName.isBlank() ||
                        location.regionName.equals("unknown", ignoreCase = true)
                    ) {
                        if (location.areas.isEmpty() && location.gameIndices.isEmpty()) {
                            Text(
                                text = "NO ADDITIONAL LOCATION DATA AVAILABLE.",
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationStatRow(label: String, value: String) {
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
