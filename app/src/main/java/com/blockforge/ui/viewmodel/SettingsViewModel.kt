package com.blockforge.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blockforge.data.database.BlockingSettings
import com.blockforge.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing blocking settings
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    /**
     * Current blocking settings
     */
    val settings: StateFlow<BlockingSettings> = repository.getSettings()
        .map { it ?: BlockingSettings() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BlockingSettings()
        )

    /**
     * Toggle block unknown numbers setting
     */
    fun toggleBlockUnknown(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleBlockUnknown(enabled)
        }
    }

    /**
     * Toggle block all calls setting
     */
    fun toggleBlockAll(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleBlockAll(enabled)
        }
    }

    /**
     * Toggle block international calls setting
     */
    fun toggleBlockInternational(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleBlockInternational(enabled)
        }
    }

    /**
     * Update user's country code
     */
    fun updateCountryCode(countryCode: String) {
        viewModelScope.launch {
            repository.updateUserCountryCode(countryCode)
        }
    }
}
