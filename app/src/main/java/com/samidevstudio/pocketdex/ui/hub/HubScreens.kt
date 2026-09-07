package com.samidevstudio.pocketdex.ui.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
    val accent: Color,
    val tag: String? = null
)

@Composable
fun ItemsScreen() {
    HubContentScreen(
        title = "THE BAG",
        subtitle = "Trainer essentials and quick-use tools.",
        items = listOf(
            HubItem("Potion", "Restore 20 HP from a fully stocked trainer bag.", Color(0xFF2E7D32), "heal"),
            HubItem("Great Ball", "Improved capture odds for elusive wild Pokémon.", Color(0xFF1565C0), "capture"),
            HubItem("Revive", "Bring a fainted partner back to the fight.", Color(0xFF6D4C41), "support")
        )
    )
}

@Composable
fun MovesScreen() {
    HubContentScreen(
        title = "MOVE-DEX",
        subtitle = "Strong picks, high impact, and sharp coverage.",
        items = listOf(
            HubItem("Thunderbolt", "A high-power electric attack with strong coverage.", Color(0xFFF9A825), "electric"),
            HubItem("Moonblast", "A graceful fairy move with solid damage output.", Color(0xFFAB47BC), "fairy"),
            HubItem("Earthquake", "Classic ground damage that hits many foes.", Color(0xFF8D6E63), "ground")
        )
    )
}

@Composable
fun StrategyScreen() {
    HubContentScreen(
        title = "STRATEGY",
        subtitle = "The big picture behind every battle plan.",
        items = listOf(
            HubItem("Type Matchups", "Use matchup charts to plan strong counters.", Color(0xFFE3350D), "battle"),
            HubItem("Offense Balance", "Pair attack power with coverage to pressure teams.", Color(0xFFEF6C00), "tempo"),
            HubItem("Defensive Pairing", "Build teams with anti-sweep coverage and recovery.", Color(0xFF00897B), "team")
        )
    )
}

@Composable
private fun HubContentScreen(
    title: String,
    subtitle: String,
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Text(
                text = subtitle,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .retroBorder()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NO DATA YET",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Check back soon for more notes.",
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
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
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.title.uppercase(),
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = item.accent,
                                        fontSize = 18.sp
                                    )
                                    if (!item.tag.isNullOrBlank()) {
                                        Text(
                                            text = item.tag.uppercase(),
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
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
}
