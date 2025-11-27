package com.blockforge.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blockforge.data.database.BlockedCall
import com.blockforge.data.database.BlockedPrefix
import com.blockforge.data.repository.BlocklistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing the blocklist and call logs
 */
@HiltViewModel
class BlocklistViewModel @Inject constructor(
    private val repository: BlocklistRepository
) : ViewModel() {

    /**
     * List of all blocked prefixes
     */
    val blockedPrefixes: StateFlow<List<BlockedPrefix>> = repository.getAllPrefixes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Recent blocked calls log
     */
    val recentBlockedCalls: StateFlow<List<BlockedCall>> = repository.getRecentBlockedCalls()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Total blocked calls count
     */
    val totalBlockedCount: StateFlow<Int> = repository.getBlockedCallsCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    /**
     * Blocked calls count today
     */
    val todayBlockedCount: StateFlow<Int> = repository.getBlockedCallsCountToday()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Add a new prefix to the blocklist
     */
    fun addPrefix(prefix: String, description: String? = null) {
        viewModelScope.launch {
            if (prefix.isBlank()) {
                _errorMessage.value = "Prefix cannot be empty"
                return@launch
            }

            if (repository.prefixExists(prefix.trim())) {
                _errorMessage.value = "This prefix already exists"
                return@launch
            }

            repository.addPrefix(prefix.trim(), description?.trim())
            _errorMessage.value = null
        }
    }

    /**
     * Delete a prefix from the blocklist
     */
    fun deletePrefix(prefix: BlockedPrefix) {
        viewModelScope.launch {
            repository.deletePrefix(prefix)
        }
    }

    /**
     * Clear all blocked call logs
     */
    fun clearBlockedCallLogs() {
        viewModelScope.launch {
            repository.clearBlockedCallLogs()
        }
    }

    /**
     * Delete a single blocked call log entry
     */
    fun deleteBlockedCall(call: BlockedCall) {
        viewModelScope.launch {
            repository.deleteBlockedCall(call)
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
