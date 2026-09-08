package com.samidevstudio.pocketdex.ui.types

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samidevstudio.pocketdex.ui.components.DetailTypeBadge

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TypeDetailScreen(
    typeName: String,
    viewModel: TypeViewModel,
    onBack: () -> Unit
) {
    DisposableEffect(typeName) {
        viewModel.loadTypeDetail(typeName)
        onDispose { viewModel.loadTypeDetail(null) }
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = typeName.uppercase(),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            when (val current = state) {
                is TypeDetailUiState.Loading -> Text(
                    "LOADING...",
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
                is TypeDetailUiState.Error -> Text(
                    "ERROR: ${current.message}",
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.error
                )
                is TypeDetailUiState.Success -> {
                    val matchups = current.type.matchups
                    MatchupSection("Super-effective against", matchups.superEffectiveAgainst)
                    MatchupSection("Not very effective against", matchups.notVeryEffectiveAgainst)
                    MatchupSection("No effect against", matchups.noEffectAgainst)
                    MatchupSection("Weak to", matchups.weaknesses)
                    MatchupSection("Resists", matchups.resistances)
                    MatchupSection("Immune to", matchups.immunities)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MatchupSection(title: String, types: List<String>) {
    if (types.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title.uppercase(),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            types.forEach { type -> DetailTypeBadge(type = type) }
        }
    }
}
