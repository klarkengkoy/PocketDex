package com.samidevstudio.pocketdex.ui.types

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samidevstudio.pocketdex.ui.theme.PokemonTypeColors
import com.samidevstudio.pocketdex.ui.theme.retroBorder

@Composable
fun TypeListScreen(
    viewModel: TypeViewModel,
    onTypeClick: (TypeListModel) -> Unit
) {
    val state by viewModel.listUiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "TYPE CHART",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 26.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        when (val current = state) {
            is TypeListUiState.Loading -> Text(
                "LOADING...",
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
            is TypeListUiState.Error -> Text(
                "ERROR: ${current.message}",
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.error
            )
            is TypeListUiState.Success -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 180.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(current.types, key = { it.name }) { type ->
                        val colors = PokemonTypeColors.map[type.name.lowercase()]
                        Box(
                            modifier = Modifier
                                .retroBorder()
                                .background(colors?.first ?: MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onTypeClick(type) }
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type.name.uppercase(),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = androidx.compose.ui.graphics.Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
