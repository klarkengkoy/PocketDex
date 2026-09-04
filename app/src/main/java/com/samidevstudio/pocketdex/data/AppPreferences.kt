package com.samidevstudio.pocketdex.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

class AppPreferences(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("pocketdex_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DARK_THEME = "dark_theme"
    }

    val darkThemeFlow: Flow<Boolean> = callbackFlow {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_DARK_THEME) {
                trySend(getDarkTheme())
            }
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(getDarkTheme())

        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.conflate()

    fun getDarkTheme(): Boolean = sharedPreferences.getBoolean(KEY_DARK_THEME, false)

    fun setDarkTheme(enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_DARK_THEME, enabled)
        }
    }
}
