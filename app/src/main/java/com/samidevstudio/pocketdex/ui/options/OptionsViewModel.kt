package com.samidevstudio.pocketdex.ui.options

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * Manages global application options like themes and preferences.
 */
class OptionsViewModel : ViewModel() {
    
    var isDarkTheme: Boolean by mutableStateOf(false)
        private set

    fun toggleTheme() {
        isDarkTheme = !isDarkTheme
    }
}
