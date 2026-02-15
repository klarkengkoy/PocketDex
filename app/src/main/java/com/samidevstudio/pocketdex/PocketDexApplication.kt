package com.samidevstudio.pocketdex

import android.app.Application
import com.samidevstudio.pocketdex.data.AppContainer
import com.samidevstudio.pocketdex.data.DefaultAppContainer

class PocketDexApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}
