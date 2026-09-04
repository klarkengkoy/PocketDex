package com.samidevstudio.pocketdex

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.rememberNavBackStack
import com.samidevstudio.pocketdex.ui.components.PokeballCanvas
import com.samidevstudio.pocketdex.ui.navigation.MainNavigation
import com.samidevstudio.pocketdex.ui.navigation.PokedexRoute
import com.samidevstudio.pocketdex.ui.options.OptionsViewModel
import com.samidevstudio.pocketdex.ui.theme.PocketDexTheme
import com.samidevstudio.pocketdex.ui.theme.RetroStyles
import com.samidevstudio.pocketdex.ui.theme.retroBackground

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install the Splash Screen MUST be called before super.onCreate()
        installSplashScreen()
        
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val optionsViewModel: OptionsViewModel = viewModel(factory = OptionsViewModel.Factory)
            val backStack = rememberNavBackStack(PokedexRoute.List)
            val currentRoute = backStack.lastOrNull()
            val isDarkTheme by optionsViewModel.isDarkTheme.collectAsState()
            
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
                    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
                    Scaffold(
                        containerColor = Color.Transparent,
                        contentWindowInsets = WindowInsets(0, 0, 0, 0)
                    ) { _ ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            // 1. Main Navigation Layer
                            MainNavigation(
                                backStack = backStack,
                                optionsViewModel = optionsViewModel,
                                onBack = {
                                    if (backStack.size > 1) {
                                        backStack.removeAt(backStack.lastIndex)
                                    }
                                }
                            )

                            // 2. UNIFIED Navigation Assembly
                            // Placing these in a single Box ensures they are ALWAYS perfectly aligned
                            // regardless of device orientation or system navigation bars.
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
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
                                        
                                        // Space for the Pokeball
                                        Box(modifier = Modifier.size(pokeballSize))

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

                                // The Pokeball
                                val interactionSource = remember { MutableInteractionSource() }
                                Box(
                                    modifier = Modifier
                                        .size(pokeballSize)
                                        .align(Alignment.BottomCenter)
                                        // We offset it upwards to sit perfectly in the cradle
                                        .offset(y = -(barHeight - pokeballOffset))
                                        .clickable(
                                            interactionSource = interactionSource,
                                            indication = null
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
    }
}

@Composable
fun NavTabItem(
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (selected) Color(0xFFE3350D) else Color.Gray,
        modifier = Modifier
            .size(35.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    )
}
