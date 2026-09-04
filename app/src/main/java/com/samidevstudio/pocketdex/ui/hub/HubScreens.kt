package com.samidevstudio.pocketdex.ui.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samidevstudio.pocketdex.ui.theme.retroBorder

private data class HubItem(
    val title: String,
    val detail: String,
    val accent: Color
)

@Composable
fun ItemsScreen() {
    HubContentScreen(
        title = "THE BAG",
        items = listOf(
            HubItem("Potion", "Restore 20 HP from a fully stocked trainer bag.", Color(0xFF2E7D32)),
            HubItem("Great Ball", "Improved capture odds for elusive wild Pokémon.", Color(0xFF1565C0)),
            HubItem("Revive", "Bring a fainted partner back to the fight.", Color(0xFF6D4C41))
        )
    )
}

@Composable
fun MovesScreen() {
    HubContentScreen(
        title = "MOVE-DEX",
        items = listOf(
            HubItem("Thunderbolt", "A high-power electric attack with strong coverage.", Color(0xFFF9A825)),
            HubItem("Moonblast", "A graceful fairy move with solid damage output.", Color(0xFFAB47BC)),
            HubItem("Earthquake", "Classic ground damage that hits many foes.", Color(0xFF8D6E63))
        )
    )
}

@Composable
fun StrategyScreen() {
    HubContentScreen(
        title = "STRATEGY",
        items = listOf(
            HubItem("Type Matchups", "Use matchup charts to plan strong counters.", Color(0xFFE3350D)),
            HubItem("Offense Balance", "Pair attack power with coverage to pressure teams.", Color(0xFFEF6C00)),
            HubItem("Defensive Pairing", "Build teams with anti-sweep coverage and recovery.", Color(0xFF00897B))
        )
    )
}

@Composable
private fun HubContentScreen(
    title: String,
    items: List<HubItem>
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 180.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .retroBorder()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = item.title.uppercase(),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = item.accent,
                                fontSize = 18.sp
                            )
                            Text(
                                text = item.detail,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
