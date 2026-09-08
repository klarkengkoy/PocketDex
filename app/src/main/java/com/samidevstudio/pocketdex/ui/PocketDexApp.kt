package com.samidevstudio.pocketdex.ui

import android.content.res.Configuration
import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.rememberNavBackStack
import com.samidevstudio.pocketdex.ui.components.PokeballCanvas
import com.samidevstudio.pocketdex.ui.navigation.MainNavigation
import com.samidevstudio.pocketdex.ui.navigation.PokedexRoute
import com.samidevstudio.pocketdex.ui.theme.PokedexRed
import com.samidevstudio.pocketdex.ui.theme.PocketDexTheme
import com.samidevstudio.pocketdex.ui.theme.RetroStyles
import com.samidevstudio.pocketdex.ui.theme.rememberRetroIndication
import com.samidevstudio.pocketdex.ui.theme.retroBackground

@Composable
fun PocketDexApp() {
    val backStack = rememberNavBackStack(PokedexRoute.List)
    val currentRoute = backStack.lastOrNull()
    val isDarkTheme = isSystemInDarkTheme()
    val retroIndication = rememberRetroIndication()
    
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    // Adaptive Sizes
    val barHeight = if (isLandscape) 64.dp else 100.dp
    val pokeballSize = if (isLandscape) 70.dp else 100.dp
    val pokeballOffset = if (isLandscape) 40.dp else 70.dp
    val cradleRadius = if (isLandscape) 44.dp else 64.dp

    PocketDexTheme(darkTheme = isDarkTheme) {
        val color1 = MaterialTheme.colorScheme.surface
        val color2 = if (color1.luminance() > 0.5f) {
            Color.Black.copy(alpha = 0.05f).compositeOver(color1)
        } else {
            Color.White.copy(alpha = 0.05f).compositeOver(color1)
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .retroBackground(color1 = color1, color2 = color2),
            color = Color.Transparent
        ) {
            Scaffold(
                containerColor = Color.Transparent
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // 1. Main Navigation Layer
                    MainNavigation(
                        backStack = backStack,
                        onBack = {
                            if (backStack.size > 1) {
                                backStack.removeAt(backStack.lastIndex)
                            }
                        }
                    )

                    // 2. UNIFIED Navigation Assembly
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .align(Alignment.BottomCenter)
                    ) {
                        // The Bottom Bar
                        Surface(
                            color = Color.White,
                            shape = RetroStyles.cradleShape(cradleRadius),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(barHeight)
                                .align(Alignment.BottomCenter)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = if (isLandscape) 4.dp else 12.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                NavTabItem(
                                    icon = Icons.Default.SportsMartialArts,
                                    label = "Moves",
                                    indication = retroIndication,
                                    selected = currentRoute is PokedexRoute.Moves || currentRoute is PokedexRoute.MoveDetail,
                                    onClick = { 
                                        backStack.clear()
                                        backStack.add(PokedexRoute.Moves) 
                                    }
                                )
                                NavTabItem(
                                    icon = Icons.Default.AutoStories,
                                    label = "Types",
                                    indication = retroIndication,
                                    selected = currentRoute is PokedexRoute.Types || currentRoute is PokedexRoute.TypeDetail,
                                    onClick = { 
                                        backStack.clear()
                                        backStack.add(PokedexRoute.Types) 
                                    }
                                )
                                
                                // Space for the Pokeball
                                Box(modifier = Modifier.size(pokeballSize))

                                NavTabItem(
                                    icon = Icons.Default.Backpack,
                                    label = "Items",
                                    indication = retroIndication,
                                    selected = currentRoute is PokedexRoute.Items || currentRoute is PokedexRoute.ItemDetail,
                                    onClick = { 
                                        backStack.clear()
                                        backStack.add(PokedexRoute.Items) 
                                    }
                                )
                                NavTabItem(
                                    icon = Icons.Default.Groups,
                                    label = "Team",
                                    indication = retroIndication,
                                    selected = currentRoute is PokedexRoute.Team || currentRoute is PokedexRoute.TeamPokemonSelect,
                                    onClick = { 
                                        backStack.clear()
                                        backStack.add(PokedexRoute.Team) 
                                    }
                                )
                            }
                        }

                        // The Pokeball
                        val interactionSource = remember { MutableInteractionSource() }
                        Box(
                            modifier = Modifier
                                .size(pokeballSize)
                                .align(Alignment.BottomCenter)
                                .offset(y = -(barHeight - pokeballOffset))
                                .semantics { contentDescription = "Pokedex List" }
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = retroIndication,
                                    onClickLabel = "Open Pokedex List"
                                ) {
                                    backStack.clear()
                                    backStack.add(PokedexRoute.List)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            PokeballCanvas(
                                modifier = Modifier.fillMaxSize(),
                                isRouteActive = currentRoute is PokedexRoute.List || currentRoute is PokedexRoute.Detail
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NavTabItem(
    icon: ImageVector,
    label: String,
    indication: Indication,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (selected) PokedexRed else Color.Gray,
        modifier = Modifier
            .size(35.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = indication,
                onClickLabel = "Go to $label"
            ) { onClick() }
    )
}
