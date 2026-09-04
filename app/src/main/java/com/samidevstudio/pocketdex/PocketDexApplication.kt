package com.samidevstudio.pocketdex

import android.app.Application
import com.samidevstudio.pocketdex.data.AppContainer
import com.samidevstudio.pocketdex.data.AppPreferences
import com.samidevstudio.pocketdex.data.DefaultAppContainer

class PocketDexApplication : Application() {
    lateinit var container: AppContainer
    lateinit var appPreferences: AppPreferences

    override fun onCreate() {
        super.onCreate()
        appPreferences = AppPreferences(this)
        container = DefaultAppContainer(this)
    }
}
