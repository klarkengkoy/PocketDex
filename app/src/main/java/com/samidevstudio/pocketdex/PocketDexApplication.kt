package com.samidevstudio.pocketdex

import android.app.Application
import com.samidevstudio.pocketdex.data.AppContainer
import com.samidevstudio.pocketdex.data.DefaultAppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PocketDexApplication : Application() {
    lateinit var container: AppContainer
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)

        // Start the continuous evolution chain crawler in the background
        applicationScope.launch {
            container.pokemonRepository.startEvolutionChainCrawler()
        }
    }
}
