package com.samidevstudio.pocketdex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.samidevstudio.pocketdex.ui.navigation.MainNavigation
import com.samidevstudio.pocketdex.ui.navigation.PokedexRoute
import com.samidevstudio.pocketdex.ui.theme.PocketDexTheme

/**
 * MainActivity serves as the entry point and provides the main UI shell.
 * It delegates all screen-switching logic to the MainNavigation component.
 */
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            
            // Centralized navigation state (Source of Truth)
            // Uses PokedexRoute consolidated in the MainNavigation file
            val backStack = remember { mutableStateListOf<Any>(PokedexRoute.List) }

            PocketDexTheme(darkTheme = isDarkTheme) {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = "POCKETDEX",
                                    fontWeight = FontWeight.ExtraBold
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { isDarkTheme = !isDarkTheme }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = "Toggle Dark Mode"
                            )
                        }
                    }
                ) { innerPadding ->
                    Surface(modifier = Modifier.padding(innerPadding)) {
                        MainNavigation(
                            backStack = backStack,
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