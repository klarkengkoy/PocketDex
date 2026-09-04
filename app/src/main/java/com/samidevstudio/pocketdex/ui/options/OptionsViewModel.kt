package com.samidevstudio.pocketdex.ui.options

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.samidevstudio.pocketdex.PocketDexApplication
import com.samidevstudio.pocketdex.data.AppPreferences
import com.samidevstudio.pocketdex.data.PokemonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages global application options like themes and preferences.
 */
class OptionsViewModel(
    private val appPreferences: AppPreferences,
    private val repository: PokemonRepository
) : ViewModel() {

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    init {
        viewModelScope.launch {
            appPreferences.darkThemeFlow.collect { darkTheme ->
                _isDarkTheme.value = darkTheme
            }
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val nextValue = !_isDarkTheme.value
            appPreferences.setDarkTheme(nextValue)
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            repository.clearCache()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PocketDexApplication)
                OptionsViewModel(
                    appPreferences = application.appPreferences,
                    repository = application.container.pokemonRepository
                )
            }
        }
    }
}
