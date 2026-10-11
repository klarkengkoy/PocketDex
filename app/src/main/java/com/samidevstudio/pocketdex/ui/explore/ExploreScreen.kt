package com.samidevstudio.pocketdex.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samidevstudio.pocketdex.ui.components.PocketDexHeader
import com.samidevstudio.pocketdex.ui.theme.rememberRetroIndication
import com.samidevstudio.pocketdex.ui.theme.retroBorder

import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Psychology

@Composable
fun ExploreScreen(
    onTypesClick: () -> Unit,
    onAbilitiesClick: () -> Unit,
    onBerriesClick: () -> Unit,
    onRegionsClick: () -> Unit,
    onEvolutionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PocketDexHeader(text = "EXPLORE", modifier = Modifier.padding(horizontal = 0.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 180.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ExploreCategoryCard(
                    title = "TYPES",
                    subtitle = "Browse elemental types and damage matchups",
                    icon = Icons.Default.AutoStories,
                    onClick = onTypesClick
                )
            }
            item {
                ExploreCategoryCard(
                    title = "ABILITIES",
                    subtitle = "Browse Pokémon abilities and passive effects",
                    icon = Icons.Default.Psychology,
                    onClick = onAbilitiesClick
                )
            }
            item {
                ExploreCategoryCard(
                    title = "BERRIES",
                    subtitle = "Browse Pokémon berries, flavors, and growth stats",
                    icon = Icons.Default.Eco,
                    onClick = onBerriesClick
                )
            }
            item {
                ExploreCategoryCard(
                    title = "REGIONS",
                    subtitle = "Browse Pokémon world regions and Pokédexes",
                    icon = Icons.Default.Public,
                    onClick = onRegionsClick
                )
            }
            item {
                ExploreCategoryCard(
                    title = "EVOLUTIONS",
                    subtitle = "Lookup Pokémon evolution chains and branched families",
                    icon = Icons.Default.AccountTree,
                    onClick = onEvolutionsClick
                )
            }
        }
    }
}

@Composable
private fun ExploreCategoryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .retroBorder()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRetroIndication(),
                onClick = onClick
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column {
                    Text(
                        text = title.uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}
