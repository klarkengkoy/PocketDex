package com.samidevstudio.pocketdex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.samidevstudio.pocketdex.ui.PocketDexApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install the Splash Screen MUST be called before super.onCreate()
        installSplashScreen()
        
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            PocketDexApp()
        }
    }
}
