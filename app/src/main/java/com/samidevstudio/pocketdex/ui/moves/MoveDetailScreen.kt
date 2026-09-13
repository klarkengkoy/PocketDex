package com.samidevstudio.pocketdex.ui.moves

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samidevstudio.pocketdex.ui.components.DetailTypeBadge

@Composable
fun MoveDetailScreen(
    moveId: String,
    moveName: String,
    viewModel: MoveViewModel,
    onBack: () -> Unit
) {
    DisposableEffect(moveId) {
        viewModel.loadMoveDetail(moveId)
        onDispose { viewModel.loadMoveDetail(null) }
    }

    val state by viewModel.detailUiState.collectAsStateWithLifecycle()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = innerPadding.calculateTopPadding() + 24.dp, start = 16.dp, end = 16.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = moveName.replace('-', ' ').uppercase(),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            when (val current = state) {
                is MoveDetailUiState.Loading -> Text(
                    "LOADING...",
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
                is MoveDetailUiState.Error -> Text(
                    "ERROR: ${current.message}",
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.error
                )
                is MoveDetailUiState.Success -> {
                    val move = current.move
                    DetailTypeBadge(type = move.typeName)

                    MoveStatRow("Damage Category", move.damageClass.replaceFirstChar { it.uppercase() })
                    MoveStatRow("Power", move.power?.toString() ?: "—")
                    MoveStatRow("Accuracy", move.accuracy?.let { "$it%" } ?: "—")
                    MoveStatRow("PP", move.pp?.toString() ?: "—")
                    MoveStatRow("Priority", move.priority.toString())
                    move.effectChance?.let { MoveStatRow("Effect Chance", "$it%") }

                    if (move.effect.isNotBlank()) {
                        Text(
                            text = "EFFECT",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = move.effect,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MoveStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label.uppercase(),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
