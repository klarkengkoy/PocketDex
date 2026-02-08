package com.samidevstudio.pocketdex

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.samidevstudio.pocketdex.ui.components.PokeballCanvas
import com.samidevstudio.pocketdex.ui.navigation.MainNavigation
import com.samidevstudio.pocketdex.ui.navigation.PokedexRoute
import com.samidevstudio.pocketdex.ui.theme.PocketDexTheme
import com.samidevstudio.pocketdex.ui.theme.RetroStyles
import com.samidevstudio.pocketdex.ui.options.OptionsViewModel

/**
 * MainActivity serves as the entry point.
 * Hosts the 5-tab "Pokeball Hub" structure with immersive layering.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            // OptionsViewModel now found in .ui.options
            val optionsViewModel: OptionsViewModel = viewModel()
            val backStack = remember { mutableStateListOf<Any>(PokedexRoute.List) }
            val currentRoute = backStack.lastOrNull()

            PocketDexTheme(darkTheme = optionsViewModel.isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
                    Scaffold(
                        containerColor = Color.Transparent,
                        bottomBar = {
                            Surface(
                                color = Color.White,
                                shape = RetroStyles.CradleShape,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    NavTabItem(
                                        icon = Icons.Default.Backpack,
                                        selected = currentRoute is PokedexRoute.Items,
                                        onClick = { 
                                            backStack.clear()
                                            backStack.add(PokedexRoute.Items) 
                                        }
                                    )
                                    NavTabItem(
                                        icon = Icons.Default.AutoStories,
                                        selected = currentRoute is PokedexRoute.Moves,
                                        onClick = { 
                                            backStack.clear()
                                            backStack.add(PokedexRoute.Moves) 
                                        }
                                    )
                                    
                                    Box(modifier = Modifier.size(100.dp))

                                    NavTabItem(
                                        icon = Icons.Default.Psychology,
                                        selected = currentRoute is PokedexRoute.Strategy,
                                        onClick = { 
                                            backStack.clear()
                                            backStack.add(PokedexRoute.Strategy) 
                                        }
                                    )
                                    NavTabItem(
                                        icon = Icons.Default.Settings,
                                        selected = currentRoute is PokedexRoute.Options,
                                        onClick = { 
                                            backStack.clear()
                                            backStack.add(PokedexRoute.Options) 
                                        }
                                    )
                                }
                            }
                        },
                        floatingActionButton = {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .offset(y = 70.dp) 
                                    .clickable {
                                        backStack.clear()
                                        backStack.add(PokedexRoute.List)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                PokeballCanvas(modifier = Modifier.fillMaxSize())
                            }
                        },
                        floatingActionButtonPosition = FabPosition.Center
                    ) { _ ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            MainNavigation(
                                backStack = backStack,
                                optionsViewModel = optionsViewModel,
                                onBack = {
                                    if (backStack.size > 1) {
                                        backStack.removeAt(backStack.lastIndex)
                                    }
                                }
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
    selected: Boolean,
    onClick: () -> Unit
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (selected) Color(0xFFE3350D) else Color.Gray,
        modifier = Modifier
            .size(35.dp)
            .clickable { onClick() }
    )
}
